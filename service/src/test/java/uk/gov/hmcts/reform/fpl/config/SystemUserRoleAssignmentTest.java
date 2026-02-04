package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class SystemUserRoleAssignmentTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Test
    void shouldAttemptToCreateSystemUpdateRole() {
        SystemUserRoleAssignment underTest = new SystemUserRoleAssignment(roleAssignmentService);

        underTest.init();

        verify(roleAssignmentService).assignSystemUserRole();
    }

    @Test
    void shouldHandleUnexpectedExceptionGracefully() {
        SystemUserRoleAssignment underTest = new SystemUserRoleAssignment(roleAssignmentService);

        doThrow(new RuntimeException("Unexpected error")).when(roleAssignmentService).assignSystemUserRole();

        underTest.init();

        verify(roleAssignmentService).assignSystemUserRole();
    }
}
