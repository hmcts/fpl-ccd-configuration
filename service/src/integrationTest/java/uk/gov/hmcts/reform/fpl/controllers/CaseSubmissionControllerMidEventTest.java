package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerMidEventTest extends AbstractControllerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    CaseSubmissionControllerMidEventTest() {
        super("case-submission");
    }

    @Test
    void shouldReturnErrorsWhenNoCaseDataIsProvided() {
        given(featureToggleService.isApplicationDocumentsEventEnabled()).willReturn(false);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnly(
            "In the orders and directions sought section:",
            "• Add the orders and directions sought",
            "In the child's details section:",
            "• Add the child's details",
            "In the respondents' details section:",
            "• Add the respondents' details",
            "In the applicant's details section:",
            "• Add the applicant's details",
            "• Add the applicant's solicitor's details",
            "In the grounds for the application section:",
            "• Add the grounds for the application",
            "In the hearing urgency section:",
            "• Add the hearing urgency details",
            "In the upload documents section:",
            "• Add social work documents, or details of when you'll send them",
            "In the allocation proposal section:",
            "• Add the allocation proposal"
        );
    }

    @Test
    void shouldNotReturnDocumentErrorsWhenApplicationDocumentEventIsToggledOn() {
        given(featureToggleService.isApplicationDocumentsEventEnabled()).willReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnly(
            "In the orders and directions sought section:",
            "• Add the orders and directions sought",
            "In the child's details section:",
            "• Add the child's details",
            "In the respondents' details section:",
            "• Add the respondents' details",
            "In the applicant's details section:",
            "• Add the applicant's details",
            "• Add the applicant's solicitor's details",
            "In the grounds for the application section:",
            "• Add the grounds for the application",
            "In the hearing urgency section:",
            "• Add the hearing urgency details",
            "In the allocation proposal section:",
            "• Add the allocation proposal"
        );
    }

    @Test
    void shouldReturnNoErrorsWhenMandatoryFieldsAreProvidedInCaseData() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(callbackRequest());

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
