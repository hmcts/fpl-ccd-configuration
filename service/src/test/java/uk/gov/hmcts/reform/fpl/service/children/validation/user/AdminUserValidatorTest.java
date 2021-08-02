package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminUserValidatorTest {

    private final UserService user = mock(UserService.class);

    private final AdminUserValidator underTest = new AdminUserValidator(user) {};

    @Test
    void acceptsUserAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(true);
        assertThat(underTest.acceptsUser()).isTrue();
    }

    @Test
    void acceptsUserNotAdmin() {
        when(user.isHmctsAdminUser()).thenReturn(false);
        assertThat(underTest.acceptsUser()).isFalse();
    }
}
