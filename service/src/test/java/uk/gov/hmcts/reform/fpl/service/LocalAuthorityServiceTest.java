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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class LocalAuthorityServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String LOCAL_AUTHORITY_CODE = "EX";

    @Mock
    private IdamApi idamApi;

    @Mock
    private LocalAuthorityCodeLookupConfiguration codeConfig;

    @Mock
    private LocalAuthorityNameLookupConfiguration nameConfig;

    @InjectMocks
    private LocalAuthorityService localAuthorityService;

    @ParameterizedTest
    @ValueSource(strings = {"mock@example.gov.uk", "mock.mock@example.gov.uk", "mock@ExAmPlE.gov.uk"})
    void shouldReturnLocalAuthorityCode(String email) {
        final String expectedLaCode = "EX";

        given(codeConfig.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder().put("example.gov.uk", LOCAL_AUTHORITY_CODE).build()
        );

        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails("1", email, "Mock", "Mock", new ArrayList<>()));

        String domain = localAuthorityService.getLocalAuthorityCode(AUTH_TOKEN);

        Assertions.assertThat(domain).isEqualTo(expectedLaCode);
    }

    @Test
    void shouldReturnExceptionWhenIdamApiThrows() {
        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willThrow(
            new RuntimeException("user does not exist"));

        assertThatThrownBy(() -> localAuthorityService.getLocalAuthorityCode(AUTH_TOKEN))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("user does not exist");
    }

    @Test
    void shouldThrowCustomExceptionWhenDomainNotFound() throws IllegalArgumentException {
        given(codeConfig.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder().put("example.gov.uk", LOCAL_AUTHORITY_CODE).build()
        );

        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails(null, "notfound@email.com", null, null, null));

        assertThatThrownBy(() -> localAuthorityService.getLocalAuthorityCode(AUTH_TOKEN))
            .isInstanceOf(UnknownLocalAuthorityDomainException.class).hasMessage("email.com not found");
    }

    @Test
    void shouldReturnLocalAuthorityName() {
        final String expectedName = "Example Local Authority";

        given(nameConfig.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder()
            .put(LOCAL_AUTHORITY_CODE, "Example Local Authority")
                .build()
        );

        String name = localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE);

        assertThat(name).isEqualTo(expectedName);
    }

    @Test
    void shouldThrowNullPointerWhenLocalAuthorityIsNull() {
        given(nameConfig.getLookupTable()).willReturn(
            ImmutableMap.<String, String>builder()
            .put(LOCAL_AUTHORITY_CODE, "Example Local Authority")
                .build()
        );

        assertThatThrownBy(() -> localAuthorityService.getLocalAuthorityName(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("No local authority found");
    }
}
