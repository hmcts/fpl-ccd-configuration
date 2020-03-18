package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
            "In the orders and directions needed section:",
            "• You need to add details to orders and directions needed",
            "In the children section:",
            "• You need to add details to children",
            "In the respondents section:",
            "• You need to add details to respondents",
            "In the applicant section:",
            "• You need to add details to applicant",
            "• You need to add details to solicitor",
            "In the grounds for the application section:",
            "• You need to add details to grounds for the application",
            "In the hearing needed section:",
            "• You need to add details to hearing needed",
            "In the documents section:",
            "• Tell us the status of all documents including those that you haven't uploaded",
            "In the allocation proposal section:",
            "• You need to add details to allocation proposal"
        );
    }

    @Test
    void shouldReturnNoErrorsWhenMandatoryFieldsAreProvidedInCaseData() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(
            "core-case-data-store-api/callback-request.json");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }
}
