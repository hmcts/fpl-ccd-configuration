package uk.gov.hmcts.reform.fpl.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;
import uk.gov.hmcts.reform.rd.model.JudicialUserRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private final CaseAccessService caseAccessService;
    private final SystemUserService systemUserService;
    private final JudicialApi judicialApi;
    private final AmApi amApi;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignAllocatedJudge(Long caseId, String userId) {
        // todo - WIP
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
                .requestedRoles(List.of(RoleAssignment.builder()
                        .actorId(userId)
                        .attributes(Map.of("caseId", caseId.toString(),
                            "caseType", CASE_TYPE,
                            "jurisdiction", JURISDICTION,
                            "substantive", "Y"))
                        .grantType(GrantType.STANDARD)
                        .roleCategory(RoleCategory.JUDICIAL)
                        .roleType(RoleType.CASE)
                        .roleName("allocated-judge")
                        .readOnly(false)
                    .build()))
                .roleRequest(RoleRequest.builder()
                    .assignerId(systemUserService.getUserId(systemUserToken))
                    .reference(caseId.toString())
                    .build())
            .build());
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

}
