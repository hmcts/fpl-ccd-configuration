package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.DeleteRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.QueryRequest;
import uk.gov.hmcts.reform.am.model.QueryResponse;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils.buildRoleAssignments;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    private final AmApi amApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Create a role assignment in AM. This will REPLACE the existing role assignment.
     *
     * @param caseId the case to assign the role on
     * @param userIds the users to assign the role to
     * @param role the role name they will be assigned, e.g. allocated-judge
     * @param roleCategory the role category, i.e. JUDICIAL, LEGAL_OPERATIONS
     * @param endTime the time the role should expire
     */
    @Retryable(value = {FeignException.class}, label = "Create case role assignment")
    public void assignCaseRole(Long caseId, List<String> userIds, String role, RoleCategory roleCategory,
                               ZonedDateTime startTime, ZonedDateTime endTime) {
        String systemUserToken = systemUserService.getSysUserToken();

        log.info("Attempting to assign role {} on case {}", role, caseId);
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
            .requestedRoles(buildRoleAssignments(caseId, userIds, role, roleCategory, startTime, endTime))
            .roleRequest(RoleRequest.builder()
                .assignerId(systemUserService.getUserId(systemUserToken))
                .reference("fpl-case-role-assignment")
                .replaceExisting(true)
                .build())
            .build());
    }

    /**
     * Create role assignments based on an already created List of RoleAssignment objects.
     * @param roleAssignments pre-created List of RoleAssignment objects
     */
    @Retryable(value = {FeignException.class}, label = "Create bulk case role assignment")
    public void createRoleAssignments(List<RoleAssignment> roleAssignments) {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.createRoleAssignment(systemUserToken, authTokenGenerator.generate(), AssignmentRequest.builder()
            .requestedRoles(roleAssignments)
            .roleRequest(RoleRequest.builder()
                .assignerId(systemUserService.getUserId(systemUserToken))
                .reference("fpl-case-role-assignment")
                .replaceExisting(true)
                .build())
            .build());

    }

    /**
     * Assign a Judicial case role to the users on the specified case, with a defined beginning and end time.
     * @param caseId case to create the role for
     * @param userIds users to create the role for
     * @param judgeRole the Judicial case role to assign
     * @param beginTime the time this role assignment should begin
     * @param endTime the time this role assignment should end
     */
    public void assignJudgesRole(Long caseId, List<String> userIds, JudgeCaseRole judgeRole,
                                 ZonedDateTime beginTime, ZonedDateTime endTime) {
        assignCaseRole(caseId, userIds, judgeRole.getRoleName(), RoleCategory.JUDICIAL, beginTime, endTime);
    }

    /**
     * Assign a Legal Operations case role to the users on the specified case, with a defined beginning and end time.
     * @param caseId case to create the role for
     * @param userIds users to create the role for
     * @param legalAdviserRole the Legal Operations case role to assign
     * @param beginTime the time this role assignment should begin
     * @param endTime the time this role assignment should end
     */
    public void assignLegalAdvisersRole(Long caseId, List<String> userIds, LegalAdviserRole legalAdviserRole,
                                        ZonedDateTime beginTime, ZonedDateTime endTime) {
        assignCaseRole(caseId, userIds, legalAdviserRole.getRoleName(), RoleCategory.LEGAL_OPERATIONS,
            beginTime, endTime);
    }

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

    /**
     * Fetch all active role assignments for a specific case, with a specific role name, at a given time.
     * @param caseId the case ID to search for roles on
     * @param roleNames the List of roles we want to find
     * @param time the time at which the role assignments should be valid at
     * @return a list of RoleAssignments matching the query
     */
    @Retryable(value = {FeignException.class}, label = "Fetch case role assignments")
    public List<RoleAssignment> getCaseRolesAtTime(Long caseId, List<String> roleNames, ZonedDateTime time) {
        String systemUserToken = systemUserService.getSysUserToken();
        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .attributes(Map.of("caseId", List.of(caseId.toString())))
                .roleName(roleNames)
                .validAt(time)
                .build()
        );
        return resp.getRoleAssignmentResponse();
    }

    @Retryable(value = {FeignException.class}, label = "Delete role assignments by query")
    public void deleteRoleAssignmentsByQuery(Long caseId, List<String> caseRoles) {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.deleteRoleAssignmentsByQuery(systemUserToken, authTokenGenerator.generate(), DeleteRequest.builder()
            .queryRequests(List.of(QueryRequest.builder()
                    .roleName(caseRoles)
                    .attributes(Map.of("caseId", List.of(caseId.toString())))
                .build()))
            .build());
    }

    @Retryable(value = {FeignException.class}, label = "Delete a single role assignment")
    public void deleteRoleAssignment(RoleAssignment roleToDelete) {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.deleteRoleAssignment(systemUserToken, authTokenGenerator.generate(), roleToDelete.getId());
    }
}
