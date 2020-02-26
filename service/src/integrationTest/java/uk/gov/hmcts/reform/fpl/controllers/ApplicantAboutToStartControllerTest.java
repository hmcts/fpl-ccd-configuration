package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantAboutToStartControllerTest extends AbstractControllerTest {

    private final String serviceAuthToken = RandomStringUtils.randomAlphanumeric(10);
    final String userAuthToken = "Bearer token";

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
        given(organisationApi.findOrganisationById(userAuthToken, serviceAuthToken)).willReturn(Organisation.builder().build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("applicants");
    }
}
