package uk.gov.hmcts.reform.fpl.controllers.applicant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.State;
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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantLocalAuthorityControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationApi organisationApi;

    private final Organisation organisation = Organisation.builder()
        .organisationIdentifier("ORG1")
        .name("ORG 1")
        .contactInformation(List.of(ContactInformation.builder()
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .postCode("AB 100")
            .build()))
        .build();

    ApplicantLocalAuthorityControllerAboutToStartTest() {
        super("enter-local-authority");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willReturn(organisation);
    }

    @Test
    void shouldThrowErrorIfNotInApplicantOrgAndInCaseManagementState() {
        final CaseData caseData = CaseData.builder()
            .state(State.CASE_MANAGEMENT)
            .localAuthorityPolicy(createPolicy("ORG2"))
            .sharedLocalAuthorityPolicy(createPolicy("ORG3"))
            .outsourcingPolicy(createPolicy("ORG4"))
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).containsExactly(
            "You must be the applicant or acting on behalf of the applicant to modify these details.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"OPEN", "RETURNED"})
    void shouldNotThrowErrorIfNotInApplicantOrgAndInOpenOrReturnedStates(String state) {
        final CaseData caseData = CaseData.builder()
            .state(State.fromValue(state))
            .localAuthorityPolicy(createPolicy("ORG2"))
            .sharedLocalAuthorityPolicy(createPolicy("ORG3"))
            .outsourcingPolicy(createPolicy("ORG4"))
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldPrePopulateLocalAuthorityDetailsFromReferenceData() {
        CaseData caseData = CaseData.builder()
            .state(State.OPEN)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final LocalAuthorityEventData expectedData = LocalAuthorityEventData.builder()
            .localAuthority(LocalAuthority.builder()
                .id(organisation.getOrganisationIdentifier())
                .name(organisation.getName())
                .address(Address.builder()
                    .addressLine1(organisation.getContactInformation().get(0).getAddressLine1())
                    .addressLine2(organisation.getContactInformation().get(0).getAddressLine2())
                    .postcode(organisation.getContactInformation().get(0).getPostCode())
                    .build())
                .build())
            .localAuthorityColleagues(emptyList())
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
            .state(State.CASE_MANAGEMENT)
            .localAuthorityPolicy(organisationPolicy(organisation.getOrganisationIdentifier(), organisation.getName(),
                LASOLICITOR))
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
                .id(organisation.getOrganisationIdentifier())
                .name(legacyApplicant.getOrganisationName())
                .colleagues(expectedColleagues)
                .build())
            .localAuthorityColleagues(expectedColleagues)
            .build();

        assertThat(updatedCaseData.getLocalAuthorityEventData()).isEqualTo(expectedEventData);
    }

    @Test
    void shouldGetExistingLocalAuthorityDetails() {

        final List<Element<Colleague>> colleagues = wrapElements(Colleague.builder()
            .role(SOCIAL_WORKER)
            .fullName("Alex Smith")
            .build());

        final LocalAuthority localAuthority = LocalAuthority.builder()
            .id(organisation.getOrganisationIdentifier())
            .email("org@test.com")
            .colleagues(colleagues)
            .build();

        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(localAuthority))
            .localAuthorityPolicy(createPolicy(organisation.getOrganisationIdentifier()))
            .state(State.CASE_MANAGEMENT)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final LocalAuthorityEventData expectedEventData = LocalAuthorityEventData.builder()
            .localAuthority(localAuthority)
            .localAuthorityColleagues(colleagues)
            .build();

        assertThat(updatedCaseData.getLocalAuthorityEventData()).isEqualTo(expectedEventData);
    }

    private OrganisationPolicy createPolicy(String orgID) {
        return OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(orgID)
                .build())
            .build();
    }

}
