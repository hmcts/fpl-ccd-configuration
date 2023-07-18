package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    private final AmApi amApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignJudgeRole(Long caseId, String userId, JudgeCaseRole judgeRole) {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
            .requestedRoles(List.of(RoleAssignment.builder()
                .actorId(userId)
                .attributes(Map.of("caseId", caseId.toString(),
                    "caseType", CASE_TYPE,
                    "jurisdiction", JURISDICTION,
                    "substantive", "Y"))
                .grantType(GrantType.SPECIFIC)
                .roleCategory(RoleCategory.JUDICIAL)
                .roleType(RoleType.CASE)
                .roleName(judgeRole.getRoleName())
                .readOnly(false)
                .build()))
            .roleRequest(RoleRequest.builder()
                .assignerId(systemUserService.getUserId(systemUserToken))
                .reference(caseId.toString())
                .build())
            .build());
    }

    public void assignSystemUserRole() {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
            .requestedRoles(List.of(RoleAssignment.builder()
                .actorId(systemUserService.getUserId(systemUserToken))
                .attributes(Map.of("jurisdiction", JURISDICTION))
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.ADMIN)
                .roleType(RoleType.ORGANISATION)
                .roleName("case-allocator")
                .readOnly(false)
                .build()))
            .roleRequest(RoleRequest.builder()
                .assignerId(systemUserService.getUserId(systemUserToken))
                .reference("fpl-case-service-system-update-user")
                .build())
            .build());

    }
}
