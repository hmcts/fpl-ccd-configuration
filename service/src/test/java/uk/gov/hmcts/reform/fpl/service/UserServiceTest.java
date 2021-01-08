package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
