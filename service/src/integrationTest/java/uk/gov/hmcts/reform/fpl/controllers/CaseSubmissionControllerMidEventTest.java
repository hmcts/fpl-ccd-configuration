package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerMidEventTest extends AbstractControllerTest {

    CaseSubmissionControllerMidEventTest() {
        super("case-submission");
    }

    @Test
    void shouldReturnErrorsWhenNoCaseDataIsProvided() {
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
            "In the your organisation's details section:",
            "• Add your organisation's details",
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
    void shouldReturnNoErrorsWhenMandatoryFieldsAreProvidedInCaseData() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(callbackRequest());

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
