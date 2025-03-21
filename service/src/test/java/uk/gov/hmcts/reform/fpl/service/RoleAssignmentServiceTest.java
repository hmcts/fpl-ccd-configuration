package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class RoleAssignmentServiceTest {

    @Mock
    private AmApi amApi;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private RoleAssignmentService underTest;

    @Captor
    private ArgumentCaptor<AssignmentRequest> assignmentRequestCaptor;

    @Nested
    class SystemUser {

        @Test
        void shouldCreateValidSystemUserRoleAssignment() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(systemUserService.getUserId("token")).thenReturn("systemUserId");

            underTest.assignSystemUserRole();

            verify(amApi).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            verifyRoleAssignmentRequest(assignmentRequestCaptor.getValue());
        }

        private void verifyRoleAssignmentRequest(AssignmentRequest req) {
            assertThat(req.getRequestedRoles()).hasSize(1);
            assertThat(req.getRequestedRoles().get(0))
                .extracting("actorId", "roleType", "roleCategory", "attributes", "roleName",
                    "grantType", "status")
                .containsExactly("systemUserId", RoleType.ORGANISATION, RoleCategory.SYSTEM,
                    Map.of("jurisdiction", "PUBLICLAW", "primaryLocation", "UK"),
                    "case-allocator", GrantType.STANDARD, "CREATE_REQUESTED");

            assertThat(req.getRoleRequest()).extracting("assignerId", "reference", "replaceExisting",
                    "process")
                .containsExactly("systemUserId", "public-law-case-allocator-system-user", true,
                    "public-law-system-users");
        }
    }

    @Nested
    class LegalAdvisers {

        @ParameterizedTest
        @EnumSource(LegalAdviserRole.class)
        void shouldCreateValidLegalAdviserRoleAssignment(LegalAdviserRole role) {
            ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
            ZonedDateTime soon = now.plusDays(2);
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(systemUserService.getUserId("token")).thenReturn("systemUserId");

            underTest.assignLegalAdvisersRole(12345L, List.of("user1"), role,
                now, soon);

            verify(amApi).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            verifyRole(assignmentRequestCaptor.getValue(), role);
        }

        private void verifyRole(AssignmentRequest req, LegalAdviserRole role) {
            assertThat(req.getRequestedRoles()).hasSize(1);
            assertThat(req.getRequestedRoles().get(0))
                .extracting("actorId", "roleType", "roleCategory", "attributes", "roleName",
                    "grantType", "status")
                .containsExactly("user1", RoleType.CASE, RoleCategory.LEGAL_OPERATIONS,
                    Map.of("jurisdiction", "PUBLICLAW", "caseId", "12345",
                        "substantive", "Y", "caseType", "CARE_SUPERVISION_EPO"),
                    role.getRoleName(), GrantType.SPECIFIC, "CREATE_REQUESTED");

            assertThat(req.getRoleRequest()).extracting("assignerId", "reference", "replaceExisting",
                    "process")
                .containsExactly("systemUserId", "fpl-case-role-assignment", false,
                    "fpl-case-service");
        }
    }

    @Nested
    class Judiciary {

        @ParameterizedTest
        @EnumSource(JudgeCaseRole.class)
        void shouldCreateValidJudiciaryRoleAssignment(JudgeCaseRole role) {
            ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
            ZonedDateTime soon = now.plusDays(2);
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(systemUserService.getUserId("token")).thenReturn("systemUserId");

            underTest.assignJudgesRole(12345L, List.of("user1"), role,
                now, soon);

            verify(amApi).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            verifyRole(assignmentRequestCaptor.getValue(), role);
        }

        private void verifyRole(AssignmentRequest req, JudgeCaseRole role) {
            assertThat(req.getRequestedRoles()).hasSize(1);
            assertThat(req.getRequestedRoles().get(0))
                .extracting("actorId", "roleType", "roleCategory", "attributes", "roleName",
                    "grantType", "status")
                .containsExactly("user1", RoleType.CASE, RoleCategory.JUDICIAL,
                    Map.of("jurisdiction", "PUBLICLAW", "caseId", "12345",
                        "substantive", "Y", "caseType", "CARE_SUPERVISION_EPO"),
                    role.getRoleName(), GrantType.SPECIFIC, "CREATE_REQUESTED");

            assertThat(req.getRoleRequest()).extracting("assignerId", "reference", "replaceExisting",
                    "process")
                .containsExactly("systemUserId", "fpl-case-role-assignment", false,
                    "fpl-case-service");
        }
    }

    @Nested
    class CreateRoles {

        @Test
        void shouldDoNothingIfNoRoles() {
            underTest.createRoleAssignments(List.of());

            verifyNoInteractions(amApi);
        }

        @Test
        void shouldCreateAllRolesInOneRequest() {
            when(systemUserService.getUserId(any())).thenReturn("1234");
            List<RoleAssignment> roles = List.of(
                RoleAssignment.builder().build()
            );

            underTest.createRoleAssignments(roles);

            verify(amApi).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            assertThat(assignmentRequestCaptor.getValue().getRequestedRoles()).isEqualTo(roles);
            assertThat(assignmentRequestCaptor.getValue().getRoleRequest()).isEqualTo(RoleRequest.builder()
                    .assignerId("1234")
                    .reference("fpl-case-role-assignment")
                    .replaceExisting(false)
                .build());
        }

        @Test
        void shouldFallbackAndGrantIndividuallyIfBulkFails() {
            when(systemUserService.getUserId(any())).thenReturn("1234");
            List<RoleAssignment> roles = List.of(
                RoleAssignment.builder().attributes(Map.of("caseId", "1-2-3-4")).build(),
                RoleAssignment.builder().attributes(Map.of("caseId", "1-2-3-4")).build(),
                RoleAssignment.builder().attributes(Map.of("caseId", "1-2-3-4")).build(),
                RoleAssignment.builder().attributes(Map.of("caseId", "1-2-3-4")).build()
            );

            AssignmentRequest badRequest = AssignmentRequest.builder()
                .requestedRoles(roles)
                .roleRequest(RoleRequest.builder()
                    .assignerId("1234")
                    .reference("fpl-case-role-assignment")
                    .replaceExisting(false)
                    .build())
                .build();

            when(amApi.createRoleAssignment(any(), any(), eq(badRequest))).thenThrow(FeignException.class);


            underTest.createRoleAssignments(roles);

            // once that fails, and 4 more times in fallback
            verify(amApi, times(5)).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            assertThat(assignmentRequestCaptor.getAllValues()).hasSize(5);

            // One bulk, one containing each of the other roles
            assertThat(assignmentRequestCaptor.getAllValues()).map(AssignmentRequest::getRequestedRoles)
                .containsAll(List.of(roles, List.of(roles.get(0)), List.of(roles.get(1)), List.of(roles.get(2)),
                    List.of(roles.get(3))));

            assertThat(assignmentRequestCaptor.getAllValues().get(0).getRoleRequest()).isEqualTo(RoleRequest.builder()
                .assignerId("1234")
                .reference("fpl-case-role-assignment")
                .replaceExisting(false)
                .build());

        }

        @Test
        void shouldCreateSingleRole() {
            ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
            when(systemUserService.getUserId(any())).thenReturn("1234");
            underTest.assignCaseRole(12345L, List.of("1234"), "ROLE", RoleCategory.JUDICIAL, now, null);

            verify(amApi).createRoleAssignment(any(), any(), assignmentRequestCaptor.capture());

            assertThat(assignmentRequestCaptor.getValue().getRequestedRoles()).hasSize(1);
            assertThat(assignmentRequestCaptor.getValue().getRequestedRoles()).containsExactly(RoleAssignment.builder()
                    .actorIdType("IDAM")
                    .actorId("1234")
                    .attributes(Map.of("caseId", "12345", "caseType", "CARE_SUPERVISION_EPO",
                        "jurisdiction", "PUBLICLAW", "substantive", "Y"))
                    .grantType(GrantType.SPECIFIC)
                    .roleCategory(RoleCategory.JUDICIAL)
                    .roleName("ROLE")
                    .roleType(RoleType.CASE)
                    .beginTime(now)
                    .readOnly(false)
                .build());

            assertThat(assignmentRequestCaptor.getValue().getRoleRequest()).isEqualTo(RoleRequest.builder()
                .assignerId("1234")
                .reference("fpl-case-role-assignment")
                .replaceExisting(false)
                .build());

        }
    }

    @Test
    void shouldGetRolesAtTime() {
        when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder().build());

        ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
        underTest.getCaseRolesAtTime(12345L, List.of("test", "test2"), now);

        verify(amApi).queryRoleAssignments(any(), any(), eq(QueryRequest.builder()
            .validAt(now)
            .attributes(Map.of("caseId", List.of("12345")))
            .roleName(List.of("test", "test2"))
            .build()));
    }

    @Nested
    class DeletingRoles {

        @Captor
        private ArgumentCaptor<String> captor;

        @Test
        void shouldDeleteARoleAssignment() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(eq("token"), eq("auth"), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(List.of(RoleAssignment.builder().id("role-1").roleName("role-A").build()))
                .build());

            ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
            underTest.deleteRoleAssignmentOnCaseAtTime(12345L, now, "idamId", List.of("role-A"));

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .validAt(now)
                .attributes(Map.of("caseId", List.of("12345")))
                .actorId(List.of("idamId"))
                .roleName(List.of("role-A"))
                .build()));

            verify(amApi).deleteRoleAssignment(eq("token"), eq("auth"), eq("role-1"));
        }

        @Test
        void shouldOnlyDeleteARoleAssignmentIfOneOfRequestedRoles() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(eq("token"), eq("auth"), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(List.of(
                    RoleAssignment.builder().id("role-1").roleName("role-A").build()
                ))
                .build());

            ZonedDateTime now = ZonedDateTime.now(LONDON_TIMEZONE);
            underTest.deleteRoleAssignmentOnCaseAtTime(12345L, now, "idamId", List.of("role-A"));

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .validAt(now)
                .attributes(Map.of("caseId", List.of("12345")))
                .actorId(List.of("idamId"))
                .roleName(List.of("role-A"))
                .build()));

            verify(amApi, times(1)).deleteRoleAssignment(eq("token"), eq("auth"), eq("role-1"));
            verify(amApi, never()).deleteRoleAssignment(eq("token"), eq("auth"), eq("role-2"));
        }


        @Test
        void shouldDeleteAllRolesOnCase() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(eq("token"), eq("auth"), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(List.of(RoleAssignment.builder().id("role-1").build(),
                    RoleAssignment.builder().id("role-2").build()))
                .build());

            underTest.deleteAllRolesOnCase(12345L);

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .attributes(Map.of("caseId", List.of("12345")))
                .roleName(List.of("hearing-judge", "allocated-judge", "hearing-legal-adviser",
                    "allocated-legal-adviser"))
                .build()));

            verify(amApi, times(2)).deleteRoleAssignment(eq("token"), eq("auth"), captor.capture());
            assertThat(captor.getAllValues()).containsExactlyInAnyOrder("role-1", "role-2");
        }

        @Test
        void shouldDeleteAllHearingRolesOnCase() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(eq("token"), eq("auth"), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(List.of(RoleAssignment.builder().id("role-1").build(),
                    RoleAssignment.builder().id("role-2").build()))
                .build());

            underTest.deleteAllHearingRolesOnCase(12345L);

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .attributes(Map.of("caseId", List.of("12345")))
                .roleName(List.of("hearing-judge", "hearing-legal-adviser"))
                .build()));

            verify(amApi, times(2)).deleteRoleAssignment(eq("token"), eq("auth"), captor.capture());
            assertThat(captor.getAllValues()).containsExactlyInAnyOrder("role-1", "role-2");
        }

    }

    @Nested
    class QueryRoles {

        @ParameterizedTest
        @EnumSource(OrganisationalRole.class)
        void shouldGetOrgRolesForUser(OrganisationalRole orgRole) {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder()
                    .roleAssignmentResponse(List.of(
                        RoleAssignment.builder().roleName(orgRole.getValue()).build()
                    ))
                .build());

            Set<OrganisationalRole> orgRoles = underTest.getOrganisationalRolesForUser("1234");

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .actorId(List.of("1234"))
                .roleType(List.of(RoleType.ORGANISATION.toString()))
                .build()));

            assertThat(orgRoles).containsExactly(orgRole);
        }

        @Test
        void shouldReturnEmptySetIfNoOrgRoles() {
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder().build());

            Set<OrganisationalRole> orgRoles = underTest.getOrganisationalRolesForUser("1234");

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .actorId(List.of("1234"))
                .roleType(List.of(RoleType.ORGANISATION.toString()))
                .build()));

            assertThat(orgRoles).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"hearing-judge", "allocated-judge", "hearing-legal-adviser", "allocated-legal-adviser"})
        void shouldGetJudicialCaseRolesForUser(String role) {
            ZonedDateTime now = ZonedDateTime.now();

            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder()
                    .roleAssignmentResponse(List.of(RoleAssignment.builder().roleName(role).build()))
                .build());

            Set<String> caseRoles = underTest.getJudicialCaseRolesForUserAtTime("1234", 1L, now);

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .actorId(List.of("1234"))
                .attributes(Map.of("caseId", List.of("1")))
                .roleName(List.of("hearing-judge", "allocated-judge", "hearing-legal-adviser",
                    "allocated-legal-adviser"))
                .validAt(now)
                .build()));

            assertThat(caseRoles).containsExactly(role);
        }

        @Test
        void shouldReturnEmptyJudicialCaseRolesIfNone() {
            ZonedDateTime now = ZonedDateTime.now();

            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(List.of())
                .build());

            Set<String> caseRoles = underTest.getJudicialCaseRolesForUserAtTime("1234", 1L, now);

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .actorId(List.of("1234"))
                .attributes(Map.of("caseId", List.of("1")))
                .roleName(List.of("hearing-judge", "allocated-judge", "hearing-legal-adviser",
                    "allocated-legal-adviser"))
                .validAt(now)
                .build()));

            assertThat(caseRoles).isEmpty();
        }

        @Test
        void shouldGetJudicialCaseRolesAtTime() {
            ZonedDateTime now = ZonedDateTime.now();
            List<RoleAssignment> expectedRoles = List.of();
            when(systemUserService.getSysUserToken()).thenReturn("token");
            when(authTokenGenerator.generate()).thenReturn("auth");
            when(amApi.queryRoleAssignments(any(), any(), any())).thenReturn(QueryResponse.builder()
                .roleAssignmentResponse(expectedRoles)
                .build());

            List<RoleAssignment> roles = underTest.getJudicialCaseRolesAtTime(1L, now);

            verify(amApi).queryRoleAssignments(eq("token"), eq("auth"), eq(QueryRequest.builder()
                .attributes(Map.of("caseId", List.of("1")))
                .roleName(List.of("hearing-judge", "allocated-judge", "hearing-legal-adviser",
                    "allocated-legal-adviser"))
                .validAt(now)
                .build()));

            assertThat(roles).isEqualTo(expectedRoles);
        }

    }

}
