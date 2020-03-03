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
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(data.getAllApplicants()).contains(buildApplicant());
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
    public void shouldCatchExceptionWhenOrganisationNotFound() {
        Exception exception = new FeignException.NotFound("", REQUEST, new byte[]{});
        when(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).thenThrow(exception);

        try {
            organisationApi.findOrganisationById(userAuthToken, serviceAuthToken);
        } catch (FeignException notFound) {
            assertThat(notFound)
                .isInstanceOf(FeignException.class);
        }
    }

    private Organisation buildOrganisation() {
        return Organisation.builder().name("Organisation").build();
    }

    private Element<Applicant> buildApplicant() {
        return Element.<Applicant>builder()
            .value(Applicant.builder()
                .party(ApplicantParty.builder()
            .organisationName("Organisation")
                    .address(Address.builder()
                        .build())
                    .build())
                .build())
            .build();
    }
}
