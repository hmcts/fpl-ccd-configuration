package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.EpsLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LocalAuthorityServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String LOCAL_AUTHORITY_CODE = "EX";
    private static final String ORG_ID = "ORG001";

    @Mock
    private IdamClient idamClient;

    @Mock
    private RequestData requestData;

    @Mock
    private LocalAuthorityNameLookupConfiguration nameConfig;

    @Mock
    private LocalAuthorityCodeLookupConfiguration codeConfig;

    @Mock
    private LocalAuthorityIdLookupConfiguration idConfig;

    @Mock
    private EpsLookupConfiguration epsLookupConfiguration;

    @InjectMocks
    private LocalAuthorityService localAuthorityService;

    @BeforeEach
    void setup() {
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
    }

    @Nested
    class CurrentUserLocalAuthority {

        @ParameterizedTest
        @ValueSource(strings = {"mock@example.gov.uk", "mock.mock@example.gov.uk", "mock@ExAmPlE.gov.uk"})
        void shouldReturnLocalAuthorityCode(String email) {
            given(codeConfig.getLocalAuthorityCode("example.gov.uk")).willReturn(LOCAL_AUTHORITY_CODE);

            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(email).build());

            String domain = localAuthorityService.getLocalAuthorityCode();

            assertThat(domain).isEqualTo(LOCAL_AUTHORITY_CODE);
        }

        @Test
        void shouldReturnExceptionWhenIdamClientThrows() {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willThrow(new RuntimeException("user does not exist"));

            assertThatThrownBy(() -> localAuthorityService.getLocalAuthorityCode())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user does not exist");
        }
    }

    @Nested
    class OutsourcingLocalAuthorities {

        @Test
        void shouldReturnListOfOutsourcingLocalAuthorities() {
            when(epsLookupConfiguration.getLocalAuthorities(ORG_ID)).thenReturn(List.of("SA", "HN"));
            when(nameConfig.getLocalAuthorityName("SA")).thenReturn("Swansea City Council");
            when(nameConfig.getLocalAuthorityName("HN")).thenReturn("London Borough Hillingdon");

            final List<LocalAuthority> outsourcingLAs = localAuthorityService.getOutsourcingLocalAuthorities(ORG_ID);

            assertThat(outsourcingLAs).containsExactly(
                LocalAuthority.builder().code("SA").name("Swansea City Council").build(),
                LocalAuthority.builder().code("HN").name("London Borough Hillingdon").build());
        }

        @Test
        void shouldReturnEmptyListOfOutsourcingLocalAuthorities() {
            when(epsLookupConfiguration.getLocalAuthorities(ORG_ID)).thenReturn(emptyList());

            final List<LocalAuthority> outsourcingLAs = localAuthorityService.getOutsourcingLocalAuthorities(ORG_ID);

            assertThat(outsourcingLAs).isEmpty();
        }

        @Test
        void shouldRethrowException() {
            final Exception exception = new RuntimeException("Test");

            when(epsLookupConfiguration.getLocalAuthorities(ORG_ID)).thenReturn(List.of("SA", "HN"));
            when(nameConfig.getLocalAuthorityName("SA")).thenReturn("Swansea City Council");
            when(nameConfig.getLocalAuthorityName("HN")).thenThrow(exception);

            assertThatThrownBy(() -> localAuthorityService.getOutsourcingLocalAuthorities(ORG_ID))
                .isEqualTo(exception);
        }
    }

    @Test
    void shouldGetLocalAuthorityName() {
        when(nameConfig.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn("Swansea City Council");

        String localAuthorityName = localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityName).isEqualTo("Swansea City Council");
    }

    @Test
    void shouldGetLocalAuthorityId() {
        when(idConfig.getLocalAuthorityId(LOCAL_AUTHORITY_CODE)).thenReturn(ORG_ID);

        String localAuthorityId = localAuthorityService.getLocalAuthorityId(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityId).isEqualTo(ORG_ID);
    }

}
