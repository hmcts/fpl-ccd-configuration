package uk.gov.hmcts.reform.fpl.controllers;

import feign.FeignException;
import feign.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantAboutToStartControllerTest extends AbstractControllerTest {

    private final String serviceAuthToken = RandomStringUtils.randomAlphanumeric(10);
    final String userAuthToken = "Bearer token";
    private static final Request REQUEST = Request.create(GET, "", Map.of(), new byte[]{}, UTF_8);

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    ApplicantAboutToStartControllerTest() {
        super("enter-applicant");
    }

    @Test
    void shouldPrepopulateApplicantDataWhenNoApplicantExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken))
            .willReturn(Organisation.builder().build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("applicants");
    }

    @BeforeEach
    void setup() {
        Organisation organisation = buildOrganisation();
        given(organisationService.findOrganisation()).willReturn(organisation);
        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).willReturn(organisation);
    }

    @Test
    void shouldAddOrganisationDetailsToApplicantWhenOrganisationExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData data = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        String applicantOrganisationName = data.getAllApplicants().get(0).getValue().getParty().getOrganisationName();
        String organisationName = buildOrganisation().getName();

        assertThat(applicantOrganisationName).isEqualTo(organisationName);
    }

    @Test
    void shouldFindOrganisation() {
        Organisation organisation = buildOrganisation();
        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).willReturn(organisation);

        Organisation actualOrganisation = organisationApi.findOrganisationById(userAuthToken, serviceAuthToken);

        assertThat(actualOrganisation).isEqualTo(organisation);
    }

    @Test
    public void shouldThrowExceptionWhenOrganisationNotFound() {
        Exception exception = new FeignException.NotFound("", REQUEST, new byte[]{});
        when(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).thenThrow(exception);

        assertThatThrownBy(() -> organisationApi.findOrganisationById(userAuthToken, serviceAuthToken))
            .isInstanceOf(FeignException.class);

    }

    private Organisation buildOrganisation() {
        return Organisation.builder().name("Organisation")
        .contactInformation(buildOrganisationContactInformation())
            .build();
    }

    private ArrayList<ContactInformation> buildOrganisationContactInformation() {
        ArrayList<ContactInformation> contactInformation = new ArrayList<>();
        contactInformation.add(ContactInformation.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine1("Saffron Central")
            .county("London")
            .country("United Kingdom")
            .postCode("CR0 2GE")
            .build());

        return  contactInformation;
    }

    private Element<Applicant> buildApplicant() {
        return Element.<Applicant>builder()
            .value(Applicant.builder()
                .party(ApplicantParty.builder()
            .organisationName("Organisation")
                    .address(buildApplicantContactInformation())
                    .build())
                .build())
            .build();
    }

    private Address buildApplicantContactInformation() {
        return Address.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine1("Saffron Central")
            .county("London")
            .country("United Kingdom")
            .postcode("CR0 2GE")
            .build();
    }

}
