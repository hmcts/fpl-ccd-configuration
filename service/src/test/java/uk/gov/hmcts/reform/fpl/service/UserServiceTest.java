package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    protected IdamApi idamApi;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @ValueSource(strings = {"mock@example.gov.uk", "mock@example.com", "mock.mock@example.gov.uk"})
    void shouldReturnDomainForSuccessfulIdamCall(String email) {
        String expectedDomain = "example";
        List<String> roles = new ArrayList<>();
        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails("1", email, "Mock", "Mock", roles));

        String domain = userService.getUserDetails(AUTH_TOKEN);

        Assertions.assertThat(domain).isEqualTo(expectedDomain);
    }

    @Test
    void shouldReturnExceptionWhenIdamApiThrows() {
        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willThrow(
            new RuntimeException("user does not exist"));


        assertThatThrownBy(() -> userService.getUserDetails(AUTH_TOKEN))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("user does not exist");
    }
}
