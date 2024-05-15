package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LAMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;

@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({ApplicantService.class})
class ApplicantAboutToStartControllerTest extends AbstractCallbackTest {

    private static final Organisation POPULATED_ORGANISATION = buildOrganisation();
    private static final Organisation EMPTY_ORGANISATION = Organisation.builder().build();
    private static final String ORGANISATION_ID = "ORGSA";

    @MockBean
    private PbaNumberService pbaNumberService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private OrganisationApi organisationApi;

    ApplicantAboutToStartControllerTest() {
        super("enter-applicant");
    }

    @BeforeEach
    void setup() {
        super.setUp();
        givenSystemUser();
        givenFplService();
    }

    @WithMockUser
    @Test
    void shouldPrepopulateApplicantDataWhenNoApplicantExists() {

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willReturn(EMPTY_ORGANISATION);

        CaseData returnedCaseData = extractCaseData(postAboutToStartEvent(emptyCaseDetails()));

        Applicant expectedApplicant = Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(returnedCaseData.getApplicants().get(0).getValue().getParty().getPartyId())
                .address(Address.builder().build())
                .build())
            .build();

        assertThat(returnedCaseData.getApplicants())
            .extracting(Element::getValue)
            .containsExactly(expectedApplicant);
    }

    @WithMockUser
    @Test
    void shouldAddOrganisationDetailsToApplicantWhenOrganisationExists() {
        given(organisationService.findOrganisation()).willReturn(Optional.of(POPULATED_ORGANISATION));

        CaseData returnedCaseData = extractCaseData(postAboutToStartEvent(emptyCaseDetails()));

        ContactInformation organisationContact =
            POPULATED_ORGANISATION.getContactInformation().get(0);

        Applicant expectedApplicant = buildApplicant(returnedCaseData,
            organisationContact,
            POPULATED_ORGANISATION.getName());

        assertThat(returnedCaseData.getApplicants())
            .extracting(Element::getValue)
            .containsExactly(expectedApplicant);
    }

    @WithMockUser
    @Test
    void shouldAddManagedOrganisationDetailsToApplicant() {
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(POPULATED_ORGANISATION));

        OrganisationPolicy outsourcingPolicy = organisationPolicy("ORGEXT", null, LAMANAGING);

        OrganisationPolicy localAuthorityPolicy = organisationPolicy(ORGANISATION_ID, null, LASOLICITOR);

        CaseData caseData = CaseData.builder()
            .localAuthorityPolicy(localAuthorityPolicy)
            .outsourcingPolicy(outsourcingPolicy).build();

        CaseData returnedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        ContactInformation organisationContact =
            POPULATED_ORGANISATION.getContactInformation().get(0);

        Applicant expectedApplicant = buildApplicant(returnedCaseData,
            organisationContact,
            POPULATED_ORGANISATION.getName());

        assertThat(returnedCaseData.getApplicants())
            .extracting(Element::getValue)
            .containsExactly(expectedApplicant);
    }

    private static Applicant buildApplicant(CaseData returnedCaseData, ContactInformation organisationContact,
                                            String organisationName) {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(returnedCaseData.getApplicants().get(0).getValue().getParty().getPartyId())
                .organisationName(organisationName)
                .address(Address.builder()
                    .addressLine1(organisationContact.getAddressLine1())
                    .addressLine2(organisationContact.getAddressLine2())
                    .addressLine3(organisationContact.getAddressLine3())
                    .county(organisationContact.getCounty())
                    .country(organisationContact.getCountry())
                    .postcode(organisationContact.getPostCode())
                    .postTown(organisationContact.getTownCity())
                    .build())
                .build())
            .build();
    }

    private static Organisation buildOrganisation() {
        return Organisation.builder()
            .name("Organisation")
            .contactInformation(buildOrganisationContactInformation())
            .build();
    }

    private static List<ContactInformation> buildOrganisationContactInformation() {
        return List.of(ContactInformation.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine2("Saffron Central")
            .county("London")
            .country("United Kingdom")
            .postCode("CR0 2GE")
            .build());
    }
}
