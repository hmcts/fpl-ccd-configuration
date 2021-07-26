package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationApi organisationApi;

    ApplicantLocalAuthorityControllerAboutToStartTest() {
        super("enter-local-authority");
    }

    @BeforeEach
    void setup() {
        givenFplService();
    }

    @Test
    void shouldPrePopulateLocalAuthorityDetailsFromReferenceData() {

        final Organisation organisation = Organisation.builder()
            .name("ORG 1")
            .contactInformation(List.of(ContactInformation.builder()
                .addressLine1("Line 1")
                .addressLine2("Line 2")
                .postCode("AB 100")
                .build()))
            .build();

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willReturn(organisation);

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(emptyCaseData()));

        final LocalAuthorityEventData expectedData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .name(organisation.getName())
                .address(Address.builder()
                    .addressLine1(organisation.getContactInformation().get(0).getAddressLine1())
                    .addressLine2(organisation.getContactInformation().get(0).getAddressLine2())
                    .postcode(organisation.getContactInformation().get(0).getPostCode())
                    .build())
                .build())
            .build();

        assertThat(updatedCaseData.getLocalAuthorityEventData()).isEqualTo(expectedData);

        verify(organisationApi).findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldPopulateLocalAuthorityWithDataFromLegacyApplicant() {

        final ApplicantParty legacyApplicant = ApplicantParty.builder()
            .organisationName("Applicant")
            .build();

        final Solicitor legacySolicitor = Solicitor.builder()
            .name("Soliciotr")
            .build();

        final CaseData caseData = CaseData.builder()
            .applicants(wrapElements(Applicant.builder()
                .party(legacyApplicant)
                .build()))
            .solicitor(legacySolicitor)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final List<Element<Colleague>> expectedColleagues = wrapElements(Colleague.builder()
            .role(SOLICITOR)
            .fullName(legacySolicitor.getName())
            .mainContact("Yes")
            .notificationRecipient("Yes")
            .build());

        final LocalAuthorityEventData expectedEventData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .name(legacyApplicant.getOrganisationName())
                .colleagues(expectedColleagues)
                .build())
            .localAuthorityColleagues(expectedColleagues)
            .build();

        assertThat(updatedCaseData.getLocalAuthorityEventData()).isEqualTo(expectedEventData);

        verifyNoInteractions(organisationApi);
    }

    @Test
    void shouldGetExistingLocalAuthorityDetails() {

        final List<Element<Colleague>> colleagues = wrapElements(Colleague.builder()
            .role(SOCIAL_WORKER)
            .fullName("Alex Smith")
            .build());

        final LocalAuthority localAuthority = LocalAuthority.builder()
            .name("ORG")
            .email("org@test.com")
            .colleagues(colleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(localAuthority))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final LocalAuthorityEventData expectedEventData = LocalAuthorityEventData.builder()
            .localAuthority(localAuthority)
            .localAuthorityColleagues(colleagues)
            .build();

        assertThat(updatedCaseData.getLocalAuthorityEventData()).isEqualTo(expectedEventData);

        verifyNoInteractions(organisationApi);
    }

}
