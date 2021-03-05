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
import uk.gov.hmcts.reform.fpl.config.MlaLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.MLA;

@ExtendWith(SpringExtension.class)
class LocalAuthorityServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String LOCAL_AUTHORITY_CODE = "EX";
    private static final String ORG_ID = "ORG001";
    private static final String USER_DOMAIN = "example.gov.uk";

    @Mock
    private IdamClient idamClient;

    @Mock
    private RequestData requestData;

    @Mock
    private EpsLookupConfiguration epsConfig;

    @Mock
    private MlaLookupConfiguration mlaConfig;

    @Mock
    private LocalAuthorityIdLookupConfiguration idsConfig;

    @Mock
    private LocalAuthorityNameLookupConfiguration namesConfig;

    @Mock
    private LocalAuthorityCodeLookupConfiguration codesConfig;

    @InjectMocks
    private LocalAuthorityService underTest;

    @Nested
    class CurrentUserLocalAuthority {

        @BeforeEach
        void setup() {
            when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
        }

        @ParameterizedTest
        @ValueSource(strings = {"mock@example.gov.uk", "mock.mock@example.gov.uk", "mock@ExAmPlE.gov.uk"})
        void shouldReturnLocalAuthorityCode(String email) {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(email).build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.of(LOCAL_AUTHORITY_CODE));

            assertThat(underTest.getLocalAuthorityCode()).contains(LOCAL_AUTHORITY_CODE);
        }

        @Test
        void shouldReturnEmptyLocalAuthorityCodeWhenUserDoesNotBelongToAny() {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub("test@examlpe.gov.uk").build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.empty());

            assertThat(underTest.getLocalAuthorityCode()).isEmpty();
        }

        @Test
        void shouldRethrowsExceptions() {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willThrow(new RuntimeException("user does not exist"));

            assertThatThrownBy(() -> underTest.getLocalAuthorityCode())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user does not exist");
        }
    }

    @Nested
    class OutsourcingLocalAuthorities {

        @BeforeEach
        void init() {
            given(namesConfig.getLocalAuthorityName("SA")).willReturn("Swansea City Council");
            given(namesConfig.getLocalAuthorityName("HN")).willReturn("London Borough Hillingdon");
            given(namesConfig.getLocalAuthorityName("SN")).willReturn("Swindon Borough Council");
        }

        @Test
        void shouldReturnEmptyListOfLocalAuthoritiesRepresentedByExternalSolicitors() {
            given(epsConfig.getLocalAuthorities(ORG_ID)).willReturn(emptyList());

            List<LocalAuthority> representedLocalAuthorities = underTest.getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).isEmpty();
        }

        @Test
        void shouldReturnEmptyListOfLocalAuthoritiesRepresentedByOtherLocalAuthority() {
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(emptyList());

            List<LocalAuthority> representedLocalAuthorities = underTest.getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).isEmpty();
        }

        @Test
        void shouldReturnListOfLocalAuthoritiesRepresentedByExternalSolicitors() {
            given(epsConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SA", "HN"));
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SN"));

            List<LocalAuthority> representedLocalAuthorities = underTest.getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).containsExactlyInAnyOrder(
                LocalAuthority.builder()
                    .code("SA")
                    .name("Swansea City Council")
                    .build(),
                LocalAuthority.builder()
                    .code("HN")
                    .name("London Borough Hillingdon")
                    .build());
        }

        @Test
        void shouldReturnListOfLocalAuthoritiesRepresentedByOtherLocalAuthority() {
            given(epsConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SA", "HN"));
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SN"));

            List<LocalAuthority> representedLocalAuthorities = underTest.getOutsourcingLocalAuthorities(ORG_ID, MLA);

            assertThat(representedLocalAuthorities).containsExactlyInAnyOrder(
                LocalAuthority.builder()
                    .code("SN")
                    .name("Swindon Borough Council")
                    .build());
        }
    }

    @Test
    void shouldGetLocalAuthorityName() {
        when(namesConfig.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn("Swansea City Council");

        String localAuthorityName = underTest.getLocalAuthorityName(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityName).isEqualTo("Swansea City Council");
    }

    @Test
    void shouldGetLocalAuthorityId() {
        when(idsConfig.getLocalAuthorityId(LOCAL_AUTHORITY_CODE)).thenReturn(ORG_ID);

        String localAuthorityId = underTest.getLocalAuthorityId(LOCAL_AUTHORITY_CODE);

        assertThat(localAuthorityId).isEqualTo(ORG_ID);
    }

}
