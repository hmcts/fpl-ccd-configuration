package uk.gov.hmcts.reform.fpl.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private final CaseAccessService caseAccessService;
    private final SystemUserService systemUserService;
    private final JudicialApi judicialApi;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignAllocatedJudge(Long caseId, String userId) {
        caseAccessService.grantJudgeCaseRole(caseId, userId, ALLOCATED_JUDGE);
    }

    public void assignHearingJudge(Long caseId, String userId) {
        caseAccessService.grantJudgeCaseRole(caseId, userId, HEARING_JUDGE);
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

}
