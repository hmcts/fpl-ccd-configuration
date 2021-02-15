package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.GATEKEEPER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_SUPERUSER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;

class UserServiceTest {

    private static final String USER_AUTHORISATION = "USER_AUTH";

    private final RequestData requestData = mock(RequestData.class);
    private final IdamClient client = mock(IdamClient.class);
    private final UserService underTest = new UserService(client, requestData);

    @Test
    void shouldReturnUserEmail() {
        String expectedEmail = "user@email.com";

        when(requestData.authorisation()).thenReturn(USER_AUTHORISATION);
        when(client.getUserDetails(USER_AUTHORISATION)).thenReturn(UserDetails.builder()
            .email(expectedEmail)
            .build());

        String email = underTest.getUserEmail();

        assertThat(email).isEqualTo(expectedEmail);
    }

    @Test
    void shouldCheckIfCurrentUserHasRole() {
        when(requestData.userRoles()).thenReturn(Set.of(GATEKEEPER.getRoleName(), CAFCASS.getRoleName()));

        assertThat(underTest.hasUserRole(JUDICIARY)).isFalse();
        assertThat(underTest.hasUserRole(HMCTS_ADMIN)).isFalse();
        assertThat(underTest.hasUserRole(HMCTS_SUPERUSER)).isFalse();
        assertThat(underTest.hasUserRole(LOCAL_AUTHORITY)).isFalse();

        assertThat(underTest.hasUserRole(GATEKEEPER)).isTrue();
        assertThat(underTest.hasUserRole(CAFCASS)).isTrue();
    }

}
