package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

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
            ZonedDateTime now = ZonedDateTime.now();
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
            ZonedDateTime now = ZonedDateTime.now();
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
        void shouldCreateSingleRole() {
            ZonedDateTime now = ZonedDateTime.now();
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

}
