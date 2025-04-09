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
import uk.gov.hmcts.reform.am.model.QueryRequest;
import uk.gov.hmcts.reform.am.model.QueryResponse;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleRequest;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils.buildRoleAssignments;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    private final AmApi amApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    private static final String FPL_ROLE_ASSIGNMENT = "fpl-case-role-assignment";
    private static final String CASE_ID = "caseId";

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
                .reference(FPL_ROLE_ASSIGNMENT)
                .replaceExisting(false)
                .build())
            .build());
    }

    /**
     * Create role assignments based on an already created List of RoleAssignment objects.
     * @param roleAssignments pre-created List of RoleAssignment objects
     */
    public void createRoleAssignments(List<RoleAssignment> roleAssignments) {
        if (roleAssignments.isEmpty()) {
            log.info("No role assignments to create");
            return;
        }

        String systemUserToken = systemUserService.getSysUserToken();
        String serviceToken = authTokenGenerator.generate();

        try {
            // Attempt in BULK first, works for the majority of cases
            amApi.createRoleAssignment(systemUserToken, serviceToken, AssignmentRequest.builder()
                .requestedRoles(roleAssignments)
                .roleRequest(RoleRequest.builder()
                    .assignerId(systemUserService.getUserId(systemUserToken))
                    .reference(FPL_ROLE_ASSIGNMENT)
                    .replaceExisting(false)
                    .build())
                .build());
        } catch (Exception e) {
            log.error("Failed to bulk grant {} roles on case {}, falling back to granting each individually",
                roleAssignments.size(), roleAssignments.get(0).getAttributes().get(CASE_ID), e);

            // if we fail in bulk, we will retry each one individually - a strange workaround, but it was necessary for
            // some cases...
            roleAssignments.forEach(r -> grantIndividualRole(r, systemUserToken, serviceToken));
        }
    }

    private void grantIndividualRole(RoleAssignment role, String systemUserToken, String serviceToken) {
        try {
            log.info("Granting individual case role to {} on case {}", role.getActorId(),
                role.getAttributes().getOrDefault(CASE_ID, "no-case-id"));

            amApi.createRoleAssignment(systemUserToken, serviceToken, AssignmentRequest.builder()
                .requestedRoles(List.of(role))
                .roleRequest(RoleRequest.builder()
                    .assignerId(systemUserService.getUserId(systemUserToken))
                    .reference(FPL_ROLE_ASSIGNMENT)
                    .replaceExisting(false)
                    .build())
                .build());
        } catch (Exception e) {
            log.error("Error when granting individual case role {} to {} on case {}",
                role.getRoleName(), role.getActorId(),
                role.getAttributes().getOrDefault(CASE_ID, "no-case-id"), e);
        }
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
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .roleName(roleNames)
                .validAt(getDateTimeInUtc(time))
                .build()
        );
        return resp.getRoleAssignmentResponse();
    }

    @Retryable(value = {FeignException.class}, label = "Delete a single role assignment")
    public void deleteRoleAssignment(RoleAssignment roleToDelete) {
        String systemUserToken = systemUserService.getSysUserToken();
        amApi.deleteRoleAssignment(systemUserToken, authTokenGenerator.generate(), roleToDelete.getId());
    }

    @Retryable(value = {FeignException.class}, label = "Delete a single user's assignment on a case at time")
    public void deleteRoleAssignmentOnCaseAtTime(Long caseId, ZonedDateTime time, String userId,
                                                 List<String> roleNames) {
        String systemUserToken = systemUserService.getSysUserToken();
        String authToken = authTokenGenerator.generate();

        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .actorId(List.of(userId))
                .roleName(roleNames)
                .validAt(getDateTimeInUtc(time))
                .build()
        );

        resp.getRoleAssignmentResponse().forEach(role ->
            amApi.deleteRoleAssignment(systemUserToken, authToken, role.getId()));

        log.info("Deleted {} roles on {} case", resp.getRoleAssignmentResponse().size(), caseId);
    }

    @Retryable(value = {FeignException.class}, label = "Delete all judicial/legal adviser roles on a case")
    public void deleteAllRolesOnCase(Long caseId) {
        String systemUserToken = systemUserService.getSysUserToken();
        String authToken = authTokenGenerator.generate();

        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .roleName(List.of(HEARING_JUDGE.getRoleName(), ALLOCATED_JUDGE.getRoleName(),
                    HEARING_LEGAL_ADVISER.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName()))
                .build()
        );

        resp.getRoleAssignmentResponse().forEach(role ->
            amApi.deleteRoleAssignment(systemUserToken, authToken, role.getId()));

        log.info("Deleted {} roles on {} case", resp.getRoleAssignmentResponse().size(), caseId);
    }


    @Retryable(value = {FeignException.class}, label = "Delete all hearing judicial/legal adviser roles on a case")
    public void deleteAllHearingRolesOnCase(Long caseId) {
        String systemUserToken = systemUserService.getSysUserToken();
        String authToken = authTokenGenerator.generate();

        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .roleName(List.of(HEARING_JUDGE.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName()))
                .build()
        );

        resp.getRoleAssignmentResponse().forEach(role ->
            amApi.deleteRoleAssignment(systemUserToken, authToken, role.getId()));

        log.info("Deleted {} hearing roles on {} case", resp.getRoleAssignmentResponse().size(), caseId);
    }

    @Retryable(retryFor = {FeignException.class}, label = "Query organisation roles for user")
    public Set<OrganisationalRole> getOrganisationalRolesForUser(String userId) {
        QueryResponse response = amApi.queryRoleAssignments(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(),
            QueryRequest.builder()
                .actorId(List.of(userId))
                .roleType(List.of(RoleType.ORGANISATION.toString()))
                .build()
        );
        if (isNotEmpty(response.getRoleAssignmentResponse())) {
            return response.getRoleAssignmentResponse().stream()
                .map(role -> OrganisationalRole.from(role.getRoleName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        } else {
            return Set.of();
        }
    }

    @Retryable(retryFor = {FeignException.class}, label = "Fetch case role assignments for user")
    public Set<String> getJudicialCaseRolesForUserAtTime(String userId, Long caseId, ZonedDateTime time) {
        String systemUserToken = systemUserService.getSysUserToken();
        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .actorId(List.of(userId))
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .roleName(List.of(HEARING_JUDGE.getRoleName(), ALLOCATED_JUDGE.getRoleName(),
                    HEARING_LEGAL_ADVISER.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName()))
                .validAt(getDateTimeInUtc(time))
                .build()
        );

        if (isNotEmpty(resp.getRoleAssignmentResponse())) {
            return resp.getRoleAssignmentResponse().stream()
                .map(RoleAssignment::getRoleName)
                .collect(Collectors.toSet());
        } else {
            return Set.of();
        }
    }

    @Retryable(value = {FeignException.class}, label = "Fetch judicial case role assignments at time")
    public List<RoleAssignment> getJudicialCaseRolesAtTime(Long caseId, ZonedDateTime time) {
        String systemUserToken = systemUserService.getSysUserToken();
        QueryResponse resp = amApi.queryRoleAssignments(systemUserToken, authTokenGenerator.generate(),
            QueryRequest.builder()
                .attributes(Map.of(CASE_ID, List.of(caseId.toString())))
                .roleName(List.of(HEARING_JUDGE.getRoleName(), ALLOCATED_JUDGE.getRoleName(),
                    HEARING_LEGAL_ADVISER.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName()))
                .validAt(getDateTimeInUtc(time))
                .build()
        );
        return resp.getRoleAssignmentResponse();
    }

    static LocalDateTime getDateTimeInUtc(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
