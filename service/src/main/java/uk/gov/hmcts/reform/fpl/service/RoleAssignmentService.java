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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    private final AmApi amApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Create the role assignment for our system-update user.
     * This role can be recreated, but is likely to not be necessary. This call should be executed on startup based on
     * an environment variable.
     * This matches a pre-defined specification with AM, and if changes are needed, the drool rule on their side will
     * need updating too for the request to be successful.
     */
    @Retryable(value = {FeignException.class}, label = "Create system-user role assignment")
    public void assignSystemUserRole() {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
            .requestedRoles(List.of(RoleAssignment.builder()
                .actorId(systemUserService.getUserId(systemUserToken))
                .roleType(RoleType.ORGANISATION)
                .classification("PUBLIC")
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.SYSTEM)
                .roleName("case-allocator")
                .attributes(Map.of("jurisdiction", JURISDICTION, "primaryLocation", "UK"))
                .readOnly(false)
                .build()))
            .roleRequest(RoleRequest.builder()
                .assignerId(systemUserService.getUserId(systemUserToken))
                .reference("public-law-case-allocator-system-user")
                .process("public-law-system-users")
                .replaceExisting(true)
                .build())
            .build());
    }

}
