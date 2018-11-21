package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class LocalAuthorityNameServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private IdamApi idamApi;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityLookupConfiguration;

    @InjectMocks
    private LocalAuthorityNameService localAuthorityNameService;

    @ParameterizedTest
    @ValueSource(strings = {"mock@example.gov.uk", "mock.mock@example.gov.uk", "mock@ExAmPlE.gov.uk"})
    void shouldReturnLocalAuthorityCode(String email) {
        final String expectedLaCode = "EX";

        given(localAuthorityLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder().put("example.gov.uk", "EX").build()
        );

        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails("1", email, "Mock", "Mock", new ArrayList<>()));

        String domain = localAuthorityNameService.getLocalAuthorityCode(AUTH_TOKEN);

        Assertions.assertThat(domain).isEqualTo(expectedLaCode);
    }

    @Test
    void shouldReturnExceptionWhenIdamApiThrows() {
        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willThrow(
            new RuntimeException("user does not exist"));

        assertThatThrownBy(() -> localAuthorityNameService.getLocalAuthorityCode(AUTH_TOKEN))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("user does not exist");
    }

    @Test
    void shouldThrowExceptionWhenDomainNotFound() throws IllegalArgumentException {
        given(localAuthorityLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder().put("example.gov.uk", "EX").build()
        );

        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails(null, "notfound@email.com", null, null, null));

        assertThatThrownBy(() -> localAuthorityNameService.getLocalAuthorityCode(AUTH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("email.com not found");
    }
}
