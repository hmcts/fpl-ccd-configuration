package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalAuthorityUserValidatorTest {
    private final UserService user = mock(UserService.class);

    private final LocalAuthorityUserValidator underTest = new LocalAuthorityUserValidator(user) {};

    @Test
    void acceptsUserLocalAuthority() {
        when(user.isHmctsUser()).thenReturn(false);
        assertThat(underTest.acceptsUser()).isTrue();
    }

    @Test
    void acceptsUserHMCTS() {
        when(user.isHmctsUser()).thenReturn(true);
        assertThat(underTest.acceptsUser()).isFalse();
    }
}
