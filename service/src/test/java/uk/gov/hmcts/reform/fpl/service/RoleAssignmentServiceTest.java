package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.am.client.AmApi;
import uk.gov.hmcts.reform.am.model.AssignmentRequest;
import uk.gov.hmcts.reform.am.model.GrantType;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.am.model.RoleType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
                    Map.of("jurisdiction", "PUBLICLAW", "caseType", "CARE_SUPERVISION_EPO"),
                    "case-allocator", GrantType.STANDARD, "CREATE_REQUESTED");

            assertThat(req.getRoleRequest()).extracting("assignerId", "reference", "replaceExisting",
                    "process")
                .containsExactly("systemUserId", "public-law-case-allocator-system-user", true,
                    "public-law-system-users");
        }
    }


}
