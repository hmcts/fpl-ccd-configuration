package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private static final int HEARING_EXPIRY_OFFSET_MINS = 5;

    private final SystemUserService systemUserService;
    private final JudicialApi judicialApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final RoleAssignmentService roleAssignmentService;
    private final ValidateEmailService validateEmailService;

    /**
     * Delete a set of allocated-[users] on a specific case.
     *
     * @param caseId the case to delete allocated-[users] on
     */
    public void removeExistingAllocatedJudgesAndLegalAdvisers(Long caseId) {
        roleAssignmentService.deleteRoleAssignmentsByQuery(caseId,
            List.of(ALLOCATED_JUDGE.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName()));
    }

    /**
     * Get existing hearing-[users] who are valid at TIME = endTime, and update their
     * role assignment to last from NOW until the endTime.
     *
     * @param caseId  the caseId to update roles on
     * @param endTime the time which we don't want any existing hearing-users to have roles at
     */
    public void setExistingHearingJudgesAndLegalAdvisersToExpire(Long caseId, ZonedDateTime endTime) {
        List<RoleAssignment> judgesAndLegalAdvisers = roleAssignmentService.getCaseRolesAtTime(caseId,
            List.of(HEARING_JUDGE.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName()), endTime);

        // delete these role assignments in AM
        judgesAndLegalAdvisers.forEach(roleAssignmentService::deleteRoleAssignment);

        // loop through all role assignments, and recreate them in AM with the new endTime
        List<RoleAssignment> newRoleAssignments = judgesAndLegalAdvisers.stream()
            .map(ra -> roleAssignmentService.buildRoleAssignment(
                caseId,
                ra.getActorId(),
                ra.getRoleName(),
                ra.getRoleCategory(),
                ZonedDateTime.now(),
                endTime)
            ).collect(Collectors.toList());

        roleAssignmentService.createRoleAssignments(newRoleAssignments);
    }

    /**
     * Assign a judge case-role on a specific case, regardless of if there are any existing users with that role
     * on the case.
     *
     * @param caseId the case id to add a case role on
     * @param userId the user to add a case role to
     */
    public void assignJudgeCaseRole(Long caseId, String userId, String caseRole) {
        roleAssignmentService.assignCaseRole(caseId, List.of(userId), caseRole, RoleCategory.JUDICIAL,
            ZonedDateTime.now(), ZonedDateTime.now().plusYears(10));
    }

    /**
     * Assign a legal adviser case-role on a specific case, regardless of if there are any existing users with that role
     * on the case.
     *
     * @param caseId the case id to add a case role on
     * @param userId the user to add a case role to
     */
    public void assignLegalAdviserCaseRole(Long caseId, String userId, String caseRole) {
        roleAssignmentService.assignCaseRole(caseId, List.of(userId), caseRole, RoleCategory.LEGAL_OPERATIONS,
            ZonedDateTime.now(), ZonedDateTime.now().plusYears(10));
    }

    /**
     * Assign an allocated-judge on a case, and REMOVE all existing allocated-[users].
     *
     * @param caseId the case id to add a case role on
     * @param userId the user to assign allocated-judge to
     */
    public void assignAllocatedJudge(Long caseId, String userId) {
        // remove existing judges/legal advisers (if any) by setting their role assignment to expire now
        removeExistingAllocatedJudgesAndLegalAdvisers(caseId);

        // add new allocated judge
        roleAssignmentService.assignJudgesRole(caseId, List.of(userId), ALLOCATED_JUDGE, ZonedDateTime.now(),
            ZonedDateTime.now().plusYears(10));
    }

    /**
     * Assign an allocated-legal-adviser on a case, and REMOVE all existing allocated-[users].
     *
     * @param caseId the case id to add a case role on
     * @param userId the user to assign allocated-legal-adviser to
     */
    public void assignAllocatedLegalAdviser(Long caseId, String userId) {
        // remove existing judges/legal advisers (if any) by setting their role assignment to expire now
        removeExistingAllocatedJudgesAndLegalAdvisers(caseId);

        // add new allocated judge
        roleAssignmentService.assignLegalAdvisersRole(caseId, List.of(userId), ALLOCATED_LEGAL_ADVISER,
            ZonedDateTime.now(), ZonedDateTime.now().plusYears(10));
    }

    /**
     * Assign a hearing-judge on a case, and SET all existing hearing-[users] at that time to expire.
     *
     * @param caseId   the case id to add a case role on
     * @param userId   the user to assign hearing-judge to
     * @param starting the time to start the new role at, and the old roles to END at (- HEARING_EXPIRY_OFFSET_MINS)
     */
    public void assignHearingJudge(Long caseId, String userId, ZonedDateTime starting) {
        setExistingHearingJudgesAndLegalAdvisersToExpire(caseId, starting.minusMinutes(HEARING_EXPIRY_OFFSET_MINS));

        roleAssignmentService.assignJudgesRole(caseId, List.of(userId), HEARING_JUDGE, starting,
            ZonedDateTime.now().plusYears(10));
    }

    /**
     * Assign a hearing-legal-adviser on a case, and SET all existing hearing-[users] at that time to expire.
     *
     * @param caseId   the case id to add a case role on
     * @param userId   the user to assign hearing-legal-adviser to
     * @param starting the time to start the new role at, and the old roles to END at (- HEARING_EXPIRY_OFFSET_MINS)
     */
    public void assignHearingLegalAdviser(Long caseId, String userId, ZonedDateTime starting) {
        setExistingHearingJudgesAndLegalAdvisersToExpire(caseId, starting.minusMinutes(HEARING_EXPIRY_OFFSET_MINS));

        roleAssignmentService.assignLegalAdvisersRole(caseId, List.of(userId), HEARING_LEGAL_ADVISER, starting,
            ZonedDateTime.now().plusYears(10));
    }

    /**
     * Calls to Judicial Reference Data to check if a judge exists.
     * @param personalCode the judge's personal_code to check
     * @return if the judge exist
     */
    @Retryable(value = {FeignException.class}, label = "Check a judge exists in JRD")
    public boolean checkJudgeExists(String personalCode) {
        if (isEmpty(personalCode)) {
            return false;
        }
        String systemUserToken = systemUserService.getSysUserToken();
        List<JudicialUserProfile> judges = judicialApi.findUserByPersonalCode(systemUserToken,
            authTokenGenerator.generate(),
            JudicialUserRequest.fromPersonalCode(personalCode));

        return !judges.isEmpty();
    }

    /**
     * Validate a JudicialUser field - if they don't have a personalCode then return an error.
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
     * @param personalCode the Judicial user's personalCode
     * @return An optional JudicialUserProfile containing the details of the Judge
     */
    @Retryable(value = {FeignException.class}, label = "Search JRD for a judge by personal code")
    public Optional<JudicialUserProfile> getJudge(String personalCode) {
        if (isEmpty(personalCode)) {
            return Optional.empty();
        }
        String systemUserToken = systemUserService.getSysUserToken();
        List<JudicialUserProfile> judges = judicialApi.findUserByPersonalCode(systemUserToken,
            authTokenGenerator.generate(),
            JudicialUserRequest.fromPersonalCode(personalCode));

        if (judges.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(judges.get(0));
        }
    }

    /**
     * Lookup a specific email address in IDAM.
     * @param email the email to lookup
     * @return an Optional UUID String containing the user's idamId
     */
    @Retryable(value = {FeignException.class}, label = "Search IDAM for a UUID by email address")
    public Optional<String> getJudgeUserIdFromEmail(String email) {
        if (isEmpty(email)) {
            return Optional.empty();
        }
        List<UserDetails> users = idamClient.searchUsers(systemUserService.getSysUserToken(), "email:" + email);
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0).getId());
        }
    }

    /**
     * Gets the allocated judge on the case, and checks for a valid email address.
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
        Optional<String> error = Optional.empty();
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
        Optional<String> error = Optional.empty();
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
        Optional<String> error = Optional.empty();
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

    public void migrateHearingJudges(List<HearingBooking> hearings, Long caseId) {
        List<HearingBooking> sorted = hearings.stream()
            .sorted(Comparator.comparing(HearingBooking::getStartDate))
            .collect(Collectors.toList());

        log.info("{}", sorted.size());

        List<RoleAssignment> roles = IntStream.range(0, sorted.size())
            .mapToObj(i -> {

                HearingBooking hearing = sorted.get(i);
                log.info("{}", hearing.getStartDate());

                Optional<String> user = this.getJudgeUserIdFromEmail(hearing.getJudgeAndLegalAdvisor()
                    .getJudgeEmailAddress());

                // We have no user to assign the role to...
                if (user.isEmpty()) {
                    log.info("No user found");
                    return null;
                }

                String caseRole = hearing.getJudgeAndLegalAdvisor().getJudgeTitle().equals(LEGAL_ADVISOR)
                    ? HEARING_LEGAL_ADVISER.getRoleName()
                    : HEARING_JUDGE.getRoleName();

                RoleCategory roleCategory = hearing.getJudgeAndLegalAdvisor().getJudgeTitle().equals(LEGAL_ADVISOR)
                    ? RoleCategory.LEGAL_OPERATIONS
                    : RoleCategory.JUDICIAL;

                ZonedDateTime endDate = (i == (sorted.size() - 1))
                    ? ZonedDateTime.now().plusYears(10) // lasts 10 years if we're the newest hearing
                    : sorted.get(i + 1).getStartDate().minusMinutes(HEARING_EXPIRY_OFFSET_MINS) // else next hearing
                    .atZone(ZoneId.systemDefault());

                RoleAssignment role = roleAssignmentService.buildRoleAssignment(caseId, user.get(), caseRole,
                    roleCategory, ZonedDateTime.now(), endDate);
                log.info("{}", role);
                return role;
            })
            .filter(el -> !isEmpty(el)) // if any hearings we couldn't get a userId to assign to, filter out the nulls
            .collect(Collectors.toList());

        roleAssignmentService.createRoleAssignments(roles);
    }

}
