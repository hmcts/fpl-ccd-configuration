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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils.buildRoleAssignment;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private static final int HEARING_EXPIRY_OFFSET_MINS = 5;

    public static final int JUDICIAL_PAGE_SIZE = 3000;
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final JudicialApi judicialApi;
    private final StaffApi staffApi;
    private final RoleAssignmentService roleAssignmentService;
    private final ValidateEmailService validateEmailService;
    private final JudicialUsersConfiguration judicialUsersConfiguration;
    private final LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;
    private final ElinksService elinksService;

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
                currentTimeUK());

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
     * Assign an allocated-judge on a case, and REMOVE all existing allocated-[users].
     *
     * @param caseId the case id to add a case role on
     * @param userId the user to assign allocated-judge to
     */
    public void assignAllocatedJudge(Long caseId, String userId, boolean isLegalAdviser) {
        // remove existing judges/legal advisers (if any) by setting their role assignment to expire now
        removeExistingAllocatedJudgesAndLegalAdvisers(caseId);

        // add new allocated judge
        if (isLegalAdviser) {
            roleAssignmentService.assignLegalAdvisersRole(caseId, List.of(userId), ALLOCATED_LEGAL_ADVISER,
                currentTimeUK(), null);
        } else {
            roleAssignmentService.assignJudgesRole(caseId, List.of(userId), ALLOCATED_JUDGE, currentTimeUK(),
                null);
        }
    }

    /**
     * Assign a hearing-judge on a case, and SET all existing hearing-[users] at that time to expire.
     *
     * @param caseId   the case id to add a case role on
     * @param userId   the user to assign hearing-judge to
     * @param starting the time to start the new role at, and the old roles to END at (- HEARING_EXPIRY_OFFSET_MINS)
     */
    private void assignHearingJudgeRole(Long caseId, String userId, ZonedDateTime starting, ZonedDateTime ending,
                                       boolean isLegalAdviser) {
        setExistingHearingJudgesAndLegalAdvisersToExpire(caseId, starting.minusMinutes(HEARING_EXPIRY_OFFSET_MINS));

        if (isLegalAdviser) {
            roleAssignmentService.assignLegalAdvisersRole(caseId, List.of(userId), HEARING_LEGAL_ADVISER, starting,
                ending);
        } else {
            roleAssignmentService.assignJudgesRole(caseId, List.of(userId), HEARING_JUDGE, starting, ending);
        }
    }

    public void assignHearingJudge(Long caseId, HearingBooking hearing, Optional<HearingBooking> nextHearing,
                                   boolean startNow) {
        Optional<String> judgeId = getJudgeIdFromHearing(hearing);
        ZonedDateTime possibleEndDate = nextHearing.map(HearingBooking::getStartDate)
            .map(ld -> ld.atZone(ZONE_ID))
            .orElse(null);

        judgeId.ifPresentOrElse(s -> assignHearingJudgeRole(caseId,
                s,
                startNow ? currentTimeUK() : hearing.getStartDate().atZone(ZONE_ID),
                possibleEndDate,
                JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(hearing.getJudgeAndLegalAdvisor().getJudgeTitle())),
            () -> log.error("No judge details on hearing starting at {} on case {} to assign roles to",
                hearing.getStartDate(), caseId));
    }

    public Optional<String> getJudgeIdFromHearing(HearingBooking booking) {
        if (isEmpty(booking)
            || isEmpty(booking.getJudgeAndLegalAdvisor())
            || isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser())) {
            return Optional.empty();
        }

        if (!isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId())) {
            return Optional.of(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId());
        }

        if (!isEmpty(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getPersonalCode())) {
            return this.getJudge(booking.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getPersonalCode())
                .map(JudicialUserProfile::getSidamId);
        }
        return Optional.empty();
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
            elinksService.getElinksAcceptHeader(),
            JudicialUserRequest.fromPersonalCode(personalCode));

        return !judges.isEmpty();
    }

    /**
     * Validate a JudicialUser field - if they don't have a personalCode then return an error.
     *
     * @param judicialUser the judicial user to check
     * @return an Optional error message if the judge could not be found or was empty
     */
    public Optional<String> validateJudicialUserField(JudicialUser judicialUser) {
        if (isEmpty(judicialUser) || isEmpty(judicialUser.getPersonalCode())) {
            return Optional.of("You must search for a judge or enter their details manually");
        }

        if (!this.checkJudgeExists(judicialUser.getPersonalCode())) {
            return Optional.of("Judge could not be found, please search again or enter their details manually");
        }

        return Optional.empty();
    }

    /**
     * Attempts to retrieve a Judicial User Profile from Judicial Reference Data based on a given personalCode.
     *
     * @param personalCode the Judicial user's personalCode
     * @return An optional JudicialUserProfile containing the details of the Judge
     */
    @Retryable(value = {FeignException.class}, label = "Search JRD for a judge by personal code")
    public Optional<JudicialUserProfile> getJudge(String personalCode) {
        if (isEmpty(personalCode)) {
            return Optional.empty();
        }
        String systemUserToken = systemUserService.getSysUserToken();
        List<JudicialUserProfile> judges = judicialApi.findUsers(systemUserToken,
            authTokenGenerator.generate(),
            JUDICIAL_PAGE_SIZE,
            elinksService.getElinksAcceptHeader(),
            JudicialUserRequest.fromPersonalCode(personalCode));

        if (judges.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(judges.get(0));
        }
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

    public Optional<RoleCategory> getUserRoleCategory(String email) {
        Optional<String> judge = judicialUsersConfiguration.getJudgeUUID(email);
        Optional<String> legalAdviser = legalAdviserUsersConfiguration.getLegalAdviserUUID(email);

        if (judge.isPresent()) {
            return Optional.of(RoleCategory.JUDICIAL);
        }
        if (legalAdviser.isPresent()) {
            return Optional.of(RoleCategory.LEGAL_OPERATIONS);
        }

        return Optional.empty();
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

    // TODO - see if these can be combined/parameterised somehow
    public Optional<String> validateAllocatedJudge(CaseData caseData) {
        Optional<String> error;
        if (caseData.getEnterManually().equals(YesNo.NO)) {
            // validate judge
            error = this.validateJudicialUserField(caseData.getJudicialUser());
        } else {
            // validate manual judge details
            String email = caseData.getAllocatedJudge().getJudgeEmailAddress();
            error = validateEmailService.validate(email);
        }
        return error;
    }

    public Optional<String> validateTempAllocatedJudge(CaseData caseData) {
        Optional<String> error;
        if (caseData.getEnterManually().equals(YesNo.NO)) {
            // validate judge
            error = this.validateJudicialUserField(caseData.getJudicialUser());
        } else {
            // validate manual judge details
            String email = caseData.getTempAllocatedJudge().getJudgeEmailAddress();
            error = validateEmailService.validate(email);
        }
        return error;
    }


    public Optional<String> validateHearingJudge(CaseData caseData) {
        Optional<String> error;
        if (caseData.getEnterManuallyHearingJudge().equals(YesNo.NO)) {
            // validate judge
            error = this.validateJudicialUserField(caseData.getJudicialUserHearingJudge());
        } else {
            // validate manual judge details
            String email = caseData.getHearingJudge().getJudgeEmailAddress();
            error = validateEmailService.validate(email);
        }
        return error;
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
            .filter(time -> ObjectUtils.isEmpty(time.getEndTime()) || time.getEndTime().isAfter(currentTimeUK()))
            .map(time -> {
                Optional<RoleCategory> userRoleCategory = this.getUserRoleCategory(time.getEmailAddress());

                if (userRoleCategory.isEmpty()) {
                    return null;
                }

                boolean isLegalAdviser = userRoleCategory.get().equals(RoleCategory.LEGAL_OPERATIONS);

                return RoleAssignmentUtils.buildRoleAssignment(
                    caseData.getId(),
                    time.getJudgeId(),
                    isLegalAdviser
                        ? HEARING_LEGAL_ADVISER.getRoleName()
                        : HEARING_JUDGE.getRoleName(),
                    userRoleCategory.get(),
                    time.getStartTime(),
                    time.getEndTime() // no end date
                );
            })
            .filter(Objects::nonNull)
            .toList();
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
                hearing.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId(),
                List.of(HEARING_JUDGE.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName()));
        }
    }

    public void deleteAllRolesOnCase(Long caseId) {
        roleAssignmentService.deleteAllRolesOnCase(caseId);
    }

    public List<String> validateHearingJudgeEmail(CaseDetails caseDetails, CaseData caseData) {
        JudgeAndLegalAdvisor hearingJudge;

        final Optional<String> error = this.validateHearingJudge(caseData);

        if (error.isPresent()) {
            return List.of(error.get());
        }

        if (caseData.getEnterManuallyHearingJudge().equals(NO)) {
            // not entering manually - lookup the personal_code in JRD
            Optional<JudicialUserProfile> jup = this
                .getJudge(caseData.getJudicialUserHearingJudge().getPersonalCode());

            if (jup.isPresent()) {
                // we have managed to search the user from the personal code
                hearingJudge = JudgeAndLegalAdvisor.fromJudicialUserProfile(jup.get()).toBuilder()
                    .legalAdvisorName(caseData.getLegalAdvisorName())
                    .build();
            } else {
                return List.of("No Judge could be found, please retry your search or enter their"
                    + " details manually.");
            }
        } else {
            // entered the judge manually - lookup in our mappings and add UUID manually
            Optional<String> possibleId = this
                .getJudgeUserIdFromEmail(caseData.getHearingJudge().getJudgeEmailAddress());

            if (possibleId.isPresent()) {
                // we can manually assign the role again based on our knowledge of JRD/SRD
                hearingJudge = JudgeAndLegalAdvisor.from(caseData.getHearingJudge()).toBuilder()
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId(possibleId.get())
                        .build())
                    .legalAdvisorName(caseData.getLegalAdvisorName())
                    .build();
            } else {
                // We cannot assign manually, just have to leave the judge as is.
                hearingJudge = JudgeAndLegalAdvisor.from(caseData.getHearingJudge()).toBuilder()
                    .legalAdvisorName(caseData.getLegalAdvisorName())
                    .build();
            }
        }
        caseDetails.getData().put("judgeAndLegalAdvisor", hearingJudge);
        return List.of();
    }

    private ZonedDateTime currentTimeUK() {
        return ZonedDateTime.now(ZONE_ID);
    }
}
