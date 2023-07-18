package uk.gov.hmcts.reform.fpl.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private final SystemUserService systemUserService;
    private final JudicialApi judicialApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final RoleAssignmentService roleAssignmentService;

    public void assignAllocatedJudge(Long caseId, String userId) {
        roleAssignmentService.assignJudgeRole(caseId, userId, ALLOCATED_JUDGE);
    }

    public void assignHearingJudge(Long caseId, String userId) {
        roleAssignmentService.assignJudgeRole(caseId, userId, HEARING_JUDGE);
    }

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

    public Optional<String> validateJudicialUserField(CaseData caseData) {
        if (isEmpty(caseData.getJudicialUser()) || isEmpty(caseData.getJudicialUser().getPersonalCode())) {
            return Optional.of("You must search for a judge or enter their details manually");
        }

        if (!this.checkJudgeExists(caseData.getJudicialUser().getPersonalCode())) {
            return Optional.of("Judge could not be found, please search again or enter their details manually");
        }

        return Optional.empty();
    }



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

    @Retryable(value = {FeignException.class}, label = "Search IDAM for a UUID by email address")
    public Optional<String> getJudgeUserIdFromEmail(String email) {
        List<UserDetails> users = idamClient.searchUsers(systemUserService.getSysUserToken(), "email:" + email);
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0).getId());
        }
    }

    public Optional<String> getAllocatedJudgeEmail(CaseData caseData) {
        if (!isEmpty(caseData.getAllocatedJudge())
            && !isEmpty(caseData.getAllocatedJudge().getJudgeEmailAddress())) {
            return Optional.of(caseData.getAllocatedJudge().getJudgeEmailAddress());
        } else {
            return Optional.empty();
        }
    }

    public Set<String> getHearingJudgeEmails(CaseData caseData) {
        Set<String> hearingJudgeEmails = caseData.getAllHearings().stream()
            .filter(hearing -> !isEmpty(hearing.getValue().getJudgeAndLegalAdvisor())
                && !isEmpty(hearing.getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress()))
            .map(hearing -> hearing.getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress())
            .collect(Collectors.toSet());

        return hearingJudgeEmails;
    }

}
