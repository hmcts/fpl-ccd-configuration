package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.migration.HearingJudgeTime;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.client.StaffApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils.buildRoleAssignment;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private static final int HEARING_EXPIRY_OFFSET_MINS = 5;

    public static final int JUDICIAL_PAGE_SIZE = 3000;

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final JudicialApi judicialApi;
    private final StaffApi staffApi;
    private final RoleAssignmentService roleAssignmentService;
    private final ValidateEmailService validateEmailService;
    private final JudicialUsersConfiguration judicialUsersConfiguration;
    private final LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    /**
     * Delete a set of allocated-[users] on a specific case.
     *
     * @param caseId the case to delete allocated-[users] on
     */
    public void removeExistingAllocatedJudgesAndLegalAdvisers(Long caseId) {
        List<String> allocatedRoles = List.of(ALLOCATED_JUDGE.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName());

        List<RoleAssignment> currentAllocatedJudges = roleAssignmentService
            .getCaseRolesAtTime(caseId,
                allocatedRoles,
                ZonedDateTime.now());

        currentAllocatedJudges
            .stream()
            .filter(role -> allocatedRoles.contains(role.getRoleName()))
            .forEach(roleAssignmentService::deleteRoleAssignment);
    }

    /**
     * Get existing hearing-[users] who are valid at TIME = endTime, and update their
     * role assignment to last from NOW until the endTime.
     *
     * @param caseId  the caseId to update roles on
     * @param endTime the time which we don't want any existing hearing-users to have roles at
     */
    public void setExistingHearingJudgesAndLegalAdvisersToExpire(Long caseId, ZonedDateTime endTime) {
        List<String> hearingRoles = List.of(HEARING_JUDGE.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName());
        List<RoleAssignment> judgesAndLegalAdvisers = roleAssignmentService.getCaseRolesAtTime(caseId,
            hearingRoles, endTime);

        // delete these role assignments in AM
        judgesAndLegalAdvisers
            .stream()
            .filter(role -> hearingRoles.contains(role.getRoleName()))
            .forEach(roleAssignmentService::deleteRoleAssignment);

        // loop through all role assignments, and recreate them in AM with the new endTime
        List<RoleAssignment> newRoleAssignments = judgesAndLegalAdvisers.stream()
            .filter(role -> hearingRoles.contains(role.getRoleName()))
            .map(ra -> buildRoleAssignment(
                caseId,
                ra.getActorId(),
                ra.getRoleName(),
                ra.getRoleCategory(),
                ra.getBeginTime(),
                endTime)
            ).collect(Collectors.toList());

        roleAssignmentService.createRoleAssignments(newRoleAssignments);
    }

    /**
     * Calls to Judicial Reference Data to check if a judge exists.
     *
     * @param personalCode the judge's personal_code to check
     * @return if the judge exist
     */
    @Retryable(value = {FeignException.class}, label = "Check a judge exists in JRD")
    public boolean checkJudgeExists(String personalCode) {
        if (isEmpty(personalCode)) {
            return false;
        }
        String systemUserToken = systemUserService.getSysUserToken();
        List<JudicialUserProfile> judges = judicialApi.findUsers(systemUserToken,
            authTokenGenerator.generate(),
            JUDICIAL_PAGE_SIZE,
            JudicialUserRequest.fromPersonalCode(personalCode));

        return !judges.isEmpty();
    }

    /**
     * Lookup a specific email address in our lookup map.
     *
     * @param email the email to lookup
     * @return an Optional UUID String containing the user's idamId
     */
    @Retryable(value = {FeignException.class}, label = "Search IDAM for a UUID by email address")
    public Optional<String> getJudgeUserIdFromEmail(String email) {
        if (isEmpty(email)) {
            return Optional.empty();
        }
        Optional<String> judge = judicialUsersConfiguration.getJudgeUUID(email);
        Optional<String> legalAdviser = legalAdviserUsersConfiguration.getLegalAdviserUUID(email);

        if (judge.isPresent()) {
            return judge;
        } else {
            return legalAdviser;
        }
    }

    /**
     * Gets the allocated judge on the case, and checks for a valid email address.
     *
     * @param caseData the caseData to search through
     * @return an Optional Judge if they exist and have a valid email
     */
    public Optional<Judge> getAllocatedJudge(CaseData caseData) {
        if (!isEmpty(caseData.getAllocatedJudge())
            && !isEmpty(caseData.getAllocatedJudge().getJudgeEmailAddress())) {
            return Optional.of(caseData.getAllocatedJudge());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets the set of all hearing judges on the case, and checks each for a valid email address.
     *
     * @param caseData the caseData to search through
     * @return a Set of JudgeAndLegalAdvisor objects if they exist and have a valid email
     */
    public Set<JudgeAndLegalAdvisor> getHearingJudges(CaseData caseData) {
        return caseData.getAllHearings().stream()
            .filter(hearing -> !isEmpty(hearing.getValue().getJudgeAndLegalAdvisor())
                && !isEmpty(hearing.getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress()))
            .map(hearing -> hearing.getValue().getJudgeAndLegalAdvisor())
            .collect(Collectors.toSet());
    }

    public List<RoleAssignment> getHearingJudgeRolesForMigration(CaseData caseData) {
        List<HearingBooking> bookings = caseData.getAllNonCancelledHearings()
            .stream()
            .map(Element::getValue)
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .toList();

        List<HearingJudgeTime> judgeTimes = new ArrayList<>();
        for (int i = 0; i < bookings.size(); i++) {
            HearingBooking booking = bookings.get(i);
            HearingBooking after = (i < bookings.size() - 1) ? bookings.get(i + 1) : null;

            if (ObjectUtils.isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser())) {
                continue; // no judge UUID to grant roles on
            }

            HearingJudgeTime.HearingJudgeTimeBuilder judgeTime = HearingJudgeTime.builder()
                .emailAddress(booking.getJudgeAndLegalAdvisor().getJudgeEmailAddress())
                .judgeId(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId())
                .startTime(booking.getStartDate().atZone(ZoneId.systemDefault()))
                .judgeType(booking.getJudgeAndLegalAdvisor().getJudgeTitle());

            if (!isEmpty(after) && !isEmpty(after.getStartDate())) {
                judgeTime.endTime(after.getStartDate().atZone(ZoneId.systemDefault()));
            }

            judgeTimes.add(judgeTime.build());
        }

        return judgeTimes.stream()
            .filter(time -> ObjectUtils.isEmpty(time.getEndTime()) || time.getEndTime().isAfter(ZonedDateTime.now()))
            .map(time -> RoleAssignmentUtils.buildRoleAssignment(
                caseData.getId(),
                time.getJudgeId(),
                JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(time.getJudgeType())
                    ? HEARING_LEGAL_ADVISER.getRoleName()
                    : HEARING_JUDGE.getRoleName(),
                JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(time.getJudgeType())
                    ? RoleCategory.LEGAL_OPERATIONS
                    : RoleCategory.JUDICIAL,
                time.getStartTime(),
                time.getEndTime() // no end date
            )).toList();
    }

    public void migrateJudgeRoles(List<RoleAssignment> roles) {
        roleAssignmentService.createRoleAssignments(roles);
    }

    public void deleteSpecificHearingRole(Long caseId, HearingBooking hearing) {
        if (!isEmpty(hearing.getJudgeAndLegalAdvisor())
            && !isEmpty(hearing.getJudgeAndLegalAdvisor().getJudgeJudicialUser())
            && !isEmpty(hearing.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId())) {
            roleAssignmentService.deleteRoleAssignmentOnCaseAtTime(caseId,
                hearing.getStartDate().atZone(ZoneId.systemDefault()),
                hearing.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId());
        }
    }

    public void deleteAllRolesOnCase(Long caseId) {
        roleAssignmentService.deleteAllRolesOnCase(caseId);
    }

}
