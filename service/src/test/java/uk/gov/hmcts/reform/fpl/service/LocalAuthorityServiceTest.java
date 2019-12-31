package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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

    @Spy
    private RequestData requestData = new RequestData(AUTH_TOKEN);

    @InjectMocks
    private LocalAuthorityService localAuthorityService;

    @ParameterizedTest
    @ValueSource(strings = {"mock@example.gov.uk", "mock.mock@example.gov.uk", "mock@ExAmPlE.gov.uk"})
    void shouldReturnLocalAuthorityCode(String email) {
        final String expectedLaCode = "EX";

        given(codeConfig.getLocalAuthorityCode("example.gov.uk")).willReturn(LOCAL_AUTHORITY_CODE);

        given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(email).build());

        String domain = localAuthorityService.getLocalAuthorityCode();

        Assertions.assertThat(domain).isEqualTo(expectedLaCode);
    }

    @Test
    void shouldReturnExceptionWhenIdamApiThrows() {
        given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willThrow(
            new RuntimeException("user does not exist"));

        assertThatThrownBy(() -> localAuthorityService.getLocalAuthorityCode())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("user does not exist");
    }
}
