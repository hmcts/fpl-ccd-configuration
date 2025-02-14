package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerMidEventTest extends AbstractCallbackTest {

    CaseSubmissionControllerMidEventTest() {
        super("case-submission");
    }

    @Test
    void shouldNotReturnDocumentErrorsWhenCaseDataIsEmpty() {
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
            "• Add applicant's details",
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
