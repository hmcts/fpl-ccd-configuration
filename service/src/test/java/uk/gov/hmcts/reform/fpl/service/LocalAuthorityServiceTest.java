package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.EpsLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.MlaLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.LocalAuthorityName;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.MLA;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class LocalAuthorityServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String LOCAL_AUTHORITY_CODE = "EX";
    private static final String ORG_ID = "ORG001";
    private static final String USER_DOMAIN = "example.gov.uk";
    private static final String USER_EMAIL = "test@" + USER_DOMAIN;

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

    @Mock
    private OrganisationService organisationService;

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
        void shouldReturnLocalAuthorityCodeWhenUserEmailMappedToLocalAuthority(String email) {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(email).build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.of(LOCAL_AUTHORITY_CODE));

            assertThat(underTest.getLocalAuthorityCode()).contains(LOCAL_AUTHORITY_CODE);
        }

        @Test
        void shouldReturnEmptyLocalAuthorityCodeWhenUserDoesNotBelongToAny() {
            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(USER_EMAIL).build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.empty());
            given(organisationService.findOrganisation()).willReturn(Optional.empty());

            assertThat(underTest.getLocalAuthorityCode()).isEmpty();

            verify(codesConfig).getLocalAuthorityCode(USER_DOMAIN);
            verify(organisationService).findOrganisation();
            verifyNoInteractions(idsConfig);
        }

        @Test
        void shouldReturnEmptyWhenUserEmailNorOrganisationIsMappedToLocalAuthority() {
            String organisationId = "testId";
            Organisation organisation = Organisation.builder()
                .organisationIdentifier(organisationId)
                .build();

            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(USER_EMAIL).build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.empty());
            given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));
            given(idsConfig.getLocalAuthorityCode(organisationId)).willReturn(Optional.empty());

            assertThat(underTest.getLocalAuthorityCode()).isEmpty();

            verify(codesConfig).getLocalAuthorityCode(USER_DOMAIN);
            verify(organisationService).findOrganisation();
            verify(idsConfig).getLocalAuthorityCode(organisationId);
        }

        @Test
        void shouldReturnLocalAuthorityCodeWhenUserEmailNotMappedButUserOrganisationMappedToLocalAuthority() {
            String organisationId = "testId";
            Organisation organisation = Organisation.builder()
                .organisationIdentifier(organisationId)
                .build();

            given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(UserInfo.builder().sub(USER_EMAIL).build());
            given(codesConfig.getLocalAuthorityCode(USER_DOMAIN)).willReturn(Optional.empty());
            given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));
            given(idsConfig.getLocalAuthorityCode(organisationId)).willReturn(Optional.of(LOCAL_AUTHORITY_CODE));

            assertThat(underTest.getLocalAuthorityCode()).isEqualTo(Optional.of(LOCAL_AUTHORITY_CODE));

            verify(codesConfig).getLocalAuthorityCode(USER_DOMAIN);
            verify(organisationService).findOrganisation();
            verify(idsConfig).getLocalAuthorityCode(organisationId);
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

            List<LocalAuthorityName> representedLocalAuthorities = underTest
                .getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).isEmpty();
        }

        @Test
        void shouldReturnEmptyListOfLocalAuthoritiesRepresentedByOtherLocalAuthority() {
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(emptyList());

            List<LocalAuthorityName> representedLocalAuthorities = underTest
                .getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).isEmpty();
        }

        @Test
        void shouldReturnListOfLocalAuthoritiesRepresentedByExternalSolicitors() {
            given(epsConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SA", "HN"));
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SN"));

            List<LocalAuthorityName> representedLocalAuthorities = underTest
                .getOutsourcingLocalAuthorities(ORG_ID, EPS);

            assertThat(representedLocalAuthorities).containsExactlyInAnyOrder(
                LocalAuthorityName.builder()
                    .code("SA")
                    .name("Swansea City Council")
                    .build(),
                LocalAuthorityName.builder()
                    .code("HN")
                    .name("London Borough Hillingdon")
                    .build());
        }

        @Test
        void shouldReturnListOfLocalAuthoritiesRepresentedByOtherLocalAuthority() {
            given(epsConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SA", "HN"));
            given(mlaConfig.getLocalAuthorities(ORG_ID)).willReturn(List.of("SN"));

            List<LocalAuthorityName> representedLocalAuthorities = underTest
                .getOutsourcingLocalAuthorities(ORG_ID, MLA);

            assertThat(representedLocalAuthorities).containsExactlyInAnyOrder(
                LocalAuthorityName.builder()
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

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateLocalAuthorityFromChangeOrgRequest() {
        when(organisationService.getOrganisation("ORG456")).thenReturn(Organisation.builder()
                .companyNumber("444555666")
                .contactInformation(List.of(ContactInformation.builder()
                        .addressLine1("New Test Road")
                        .build()))
                .build());

        CaseData caseData = CaseData.builder()
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                    .organisationID("ORG123")
                    .build())
                .build())
            .localAuthorities(List.of(element(
                LocalAuthority.builder()
                    .id("ORG123")
                    .name("Joe Bloggs")
                    .email("test1@testmail.com")
                    .phone("111222333")
                    .address(Address.builder()
                        .addressLine1("Old Test Road")
                        .build())
                    .build()
            )))
            .build();

        ChangeOrganisationRequest changeOrgRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("ORG456")
                .build())
            .organisationToRemove(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("ORG123")
                .build())
            .build();

        LocalAuthority expectedUpdatedLocalAuthority = LocalAuthority.builder()
            .id("ORG456")
            .name("Joe Bloggs")
            .email("test2@testmail.com")
            .phone("444555666")
            .address(Address.builder()
                .addressLine1("New Test Road")
                .build())
            .build();

        Map<String, Object> updatedLADetails = underTest.updateLocalAuthorityFromNoC(caseData, changeOrgRequest,
            "test2@testmail.com");

        List<Element<LocalAuthority>> localAuthorities = (List<Element<LocalAuthority>>) updatedLADetails
            .get("localAuthorities");

        assertThat(localAuthorities.get(0).getValue()).isEqualTo(expectedUpdatedLocalAuthority);
    }

}
