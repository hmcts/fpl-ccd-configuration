package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantAboutToStartControllerTest extends AbstractControllerTest {

    private Organisation organisation = buildOrganisation();
    private Organisation emptyOrganisation = Organisation.builder().build();

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    ApplicantAboutToStartControllerTest() {
        super("enter-applicant");
    }

    @BeforeEach
    void setup() {
        given(organisationService.findOrganisation()).willReturn(organisation);
        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).willReturn(organisation);
    }

    @Test
    void shouldPrepopulateApplicantDataWhenNoApplicantExists() {
        CaseDetails caseDetails = buildCaseDetails();

        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken))
            .willReturn(emptyOrganisation);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("applicants");
    }

    @Test
    void shouldAddOrganisationDetailsToApplicantWhenOrganisationExists() {
        CaseDetails caseDetails = buildCaseDetails();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData data = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        String applicantOrganisationName = unwrapElements(data.getAllApplicants()).get(0)
            .getParty().getOrganisationName();

        String organisationName = organisation.getName();

        assertThat(applicantOrganisationName).isEqualTo(organisationName);
    }

    private CaseDetails buildCaseDetails() {
        return CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();
    }

    private Organisation buildOrganisation() {
        return Organisation.builder()
            .name("Organisation")
            .contactInformation(buildOrganisationContactInformation())
            .build();
    }

    private List<ContactInformation> buildOrganisationContactInformation() {
        return List.of(ContactInformation.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine1("Saffron Central")
            .county("London")
            .country("United Kingdom")
            .postCode("CR0 2GE")
            .build());
    }
}
