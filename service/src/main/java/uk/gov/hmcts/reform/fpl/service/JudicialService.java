package uk.gov.hmcts.reform.fpl.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private final AmApi amApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignAllocatedJudge(Long caseId, String userId) {
        String systemUserToken = systemUserService.getSysUserToken();
        RoleAssignmentRequestResource response = amApi.createRoleAssignment(
            systemUserToken,
            authTokenGenerator.generate(),
            AssignmentRequest.builder()
                .roleRequest(RoleRequest.builder()
                    .replaceExisting(false)
                    .assignerId(systemUserService.getUserId(systemUserToken))
                    .reference(caseId.toString())
                    .build())
                .requestedRoles(List.of(RoleAssignment.builder()
                    .actorId(userId)
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.JUDICIAL)
                    .roleName("allocated-judge")
                    .roleType(RoleType.CASE)
                    .readOnly(false)
                    .attributes(Map.of("caseId", caseId,
                        "caseType", "CARE_SUPERVISION_EPO",
                        "jurisdiction", "PUBLICLAW"))
                    .authorisations(List.of("PUBLICLAW"))
                    .build()))
                .build()
        );

        log.info("Assignment made {}", response);
    }

}
