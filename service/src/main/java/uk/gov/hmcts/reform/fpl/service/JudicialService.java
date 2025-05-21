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
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.AllocateJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.migration.HearingJudgeTime;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.client.StaffApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_HIGH_COURT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.RECORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeType.FEE_PAID_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeType.LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils.buildRoleAssignment;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private static final int HEARING_EXPIRY_OFFSET_MINS = 5;

    public static final int JUDICIAL_PAGE_SIZE = 3000;

    public static final List<JudgeOrMagistrateTitle> FEE_PAID_JUDGE_TITLES =
        List.of(DEPUTY_HIGH_COURT_JUDGE, RECORDER, DEPUTY_DISTRICT_JUDGE, DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT);
    public static final List<JudgeOrMagistrateTitle> LEGAL_ADVISOR_TITLES =
        List.of(MAGISTRATES, JudgeOrMagistrateTitle.LEGAL_ADVISOR);

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final JudicialApi judicialApi;
    private final StaffApi staffApi;
    private final RoleAssignmentService roleAssignmentService;
    private final ValidateEmailService validateEmailService;
    private final JudicialUsersConfiguration judicialUsersConfiguration;
    private final LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;
    private final ElinksService elinksService;
    private final UserService userService;

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

        // end this new role at the start of the next hearing (if present) MINUS 5 minutes to avoid overlapping roles
        ZonedDateTime possibleEndDate = nextHearing.map(HearingBooking::getStartDate)
            .map(ld -> ld.minusMinutes(HEARING_EXPIRY_OFFSET_MINS).atZone(LONDON_TIMEZONE))
            .orElse(null);

        judgeId.ifPresentOrElse(s -> assignHearingJudgeRole(caseId,
                s,
                startNow ? currentTimeUK() : hearing.getStartDate().atZone(LONDON_TIMEZONE),
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
        List<JudicialUserProfile> judges = getJudicialUserProfiles(JudicialUserRequest.fromPersonalCode(personalCode));

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
        List<JudicialUserProfile> judges = getJudicialUserProfiles(JudicialUserRequest.fromPersonalCode(personalCode));

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

    public Optional<JudgeAndLegalAdvisor> getCurrentHearingJudge(CaseData caseData) {
        return caseData.getLastHearingBefore(LocalDateTime.now())
            .map(HearingBooking::getJudgeAndLegalAdvisor);
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

    public Optional<String> validateAllocatedJudge(AllocateJudgeEventData eventData) {
        Optional<String> error;
        if (isJudgeDetailsFromJUP(eventData)) {
            // validate judge
            error = this.validateJudicialUserField(eventData.getJudicialUser());
        } else {
            // validate manual judge details
            String email = eventData.getManualJudgeDetails().getJudgeEmailAddress();
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
                .startTime(booking.getStartDate().atZone(LONDON_TIMEZONE))
                .judgeType(booking.getJudgeAndLegalAdvisor().getJudgeTitle());

            if (!isEmpty(after) && !isEmpty(after.getStartDate())) {
                judgeTime.endTime(after.getStartDate().atZone(LONDON_TIMEZONE));
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

                return buildRoleAssignment(
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

            // delete the role for this hearing + offset by 5 minutes into the future, so we don't hit the old (bugged)
            // hearing role assignments that may have ended at exactly this hearing time
            roleAssignmentService.deleteRoleAssignmentOnCaseAtTime(caseId,
                hearing.getStartDate().plusMinutes(HEARING_EXPIRY_OFFSET_MINS).atZone(LONDON_TIMEZONE),
                hearing.getJudgeAndLegalAdvisor().getJudgeJudicialUser().getIdamId(),
                List.of(HEARING_JUDGE.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName()));
        }
    }

    public void deleteAllRolesOnCase(Long caseId) {
        roleAssignmentService.deleteAllRolesOnCase(caseId);
    }

    public void cleanupHearingRoles(Long caseId) {
        roleAssignmentService.deleteAllHearingRolesOnCase(caseId);
    }

    public List<String> validateHearingJudgeEmail(CaseDetails caseDetails, CaseData caseData) {
        AllocateJudgeEventData eventData = caseData.getHearingJudgeEventData();

        final Optional<String> error = this.validateAllocatedJudge(eventData);

        if (error.isPresent()) {
            return List.of(error.get());
        }

        JudgeAndLegalAdvisor hearingJudge = this.buildAllocatedJudgeFromEventData(eventData)
            .toJudgeAndLegalAdvisor().toBuilder()
            .legalAdvisorName(caseData.getLegalAdvisorName())
            .build();

        caseDetails.getData().put("judgeAndLegalAdvisor", hearingJudge);
        return List.of();
    }

    private ZonedDateTime currentTimeUK() {
        return ZonedDateTime.now(LONDON_TIMEZONE);
    }

    public Judge buildAllocatedJudgeFromEventData(AllocateJudgeEventData eventData) {
        if (isJudgeDetailsFromJUP(eventData)) {
            if (isEmpty(eventData.getJudicialUser()) || isEmpty(eventData.getJudicialUser().getPersonalCode())) {
                return null;
            }

            return this.getJudge(eventData.getJudicialUser().getPersonalCode())
                .map(judicialUserProfile -> Judge.fromJudicialUserProfile(judicialUserProfile,
                        (FEE_PAID_JUDGE.equals(eventData.getJudgeType())) ? eventData.getFeePaidJudgeTitle() : null)
                    .toBuilder()
                    .judgeType(eventData.getJudgeType())
                    .build()
                )
                .orElse(null);
        } else {
            Judge manualJudgeDetails = eventData.getManualJudgeDetails()
                .toBuilder()
                .judgeType(eventData.getJudgeType())
                .build();

            // entering manually, check against our lookup tables, they may be a legal adviser
            Optional<String> possibleId = this.getJudgeUserIdFromEmail(manualJudgeDetails.getJudgeEmailAddress());

            // if they are in our maps - add their UUID extra info to the case
            if (possibleId.isPresent()) {
                return manualJudgeDetails.toBuilder()
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId(possibleId.get())
                        .build())
                    .build();
            } else {
                if (MAGISTRATES.equals(manualJudgeDetails.getJudgeTitle())) {
                    return manualJudgeDetails.toBuilder().judgeLastName(null).build();
                } else {
                    return manualJudgeDetails.toBuilder().judgeFullName(null).build();
                }
            }
        }
    }

    private boolean isJudgeDetailsFromJUP(AllocateJudgeEventData eventData) {
        return !LEGAL_ADVISOR.equals(eventData.getJudgeType());
    }

    public Map<String, Object> populateEventDataMapFromJudge(AbstractJudge judge) {
        Map<String, Object> resultMap = new HashMap<>();

        if (judge != null) {
            resultMap.put("judgeType", judge.getJudgeType());
            resultMap.put("judicialUser", judge.getJudgeJudicialUser());

            if (judge.isDetailsEnterManually()) {
                Map<String, Object> manualJudgeDetails = new HashMap<>();
                manualJudgeDetails.put("judgeFullName", judge.getJudgeFullName());
                manualJudgeDetails.put("judgeLastName", judge.getJudgeLastName());
                manualJudgeDetails.put("judgeEmailAddress", judge.getJudgeEmailAddress());
                if (LEGAL_ADVISOR_TITLES.contains(judge.getJudgeTitle())) {
                    manualJudgeDetails.put("judgeTitle", judge.getJudgeTitle());
                }
                resultMap.put("manualJudgeDetails", manualJudgeDetails);
            } else if (JudgeType.FEE_PAID_JUDGE.equals(judge.getJudgeType())
                       && FEE_PAID_JUDGE_TITLES.contains(judge.getJudgeTitle())) {
                resultMap.put("feePaidJudgeTitle", judge.getJudgeTitle());
            }
        }

        return resultMap;
    }

    public List<JudicialUserProfile> getJudicialUserProfiles(JudicialUserRequest request) {
        String systemUserToken = systemUserService.getSysUserToken();
        return judicialApi.findUsers(systemUserToken,
            authTokenGenerator.generate(),
            JUDICIAL_PAGE_SIZE,
            elinksService.getElinksAcceptHeader(),
            request);
    }

    @Retryable(value = {FeignException.class}, label = "Search JRD for a judge by idam id")
    public String getJudgeTitleAndNameOfCurrentUser() {
        UserDetails userDetails = userService.getUserDetails();

        List<JudicialUserProfile> judicialUserProfiles = List.of();
        try {
            judicialUserProfiles = getJudicialUserProfiles(JudicialUserRequest.builder()
                .idamId(List.of(userDetails.getId())).build());
        } catch (Exception e) {
            log.warn("Error while fetching JudicialUserProfile", e);
        }

        return judicialUserProfiles.stream().map(judicialUserProfile ->
                formatJudgeTitleAndName(JudgeAndLegalAdvisor.fromJudicialUserProfile(judicialUserProfile, null)))
            .findFirst()
            .orElse(userDetails.getFullName());
    }
}
