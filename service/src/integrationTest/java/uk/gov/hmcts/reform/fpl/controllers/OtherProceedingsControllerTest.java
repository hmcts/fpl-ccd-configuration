package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(OtherProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class OtherProceedingsControllerTest extends AbstractCallbackTest {

    private static final String ERROR_MESSAGE = "You must say if there are any other proceedings relevant to this case";

    OtherProceedingsControllerTest() {
        super("enter-other-proceedings");
    }

    @Test
    void shouldReturnWithErrorWhenOnGoingProceedingIsEmptyString() throws Exception {
        String onGoingProceeding = "";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createProceeding(onGoingProceeding));

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenPOnGoingProceedingIsYes() throws Exception {
        String onGoingProceeding = "Yes";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createProceeding(onGoingProceeding));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenPOnGoingProceedingIsNo() throws Exception {
        String onGoingProceeding = "No";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createProceeding(onGoingProceeding));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenPOnGoingProceedingIsDontKnow() throws Exception {
        String onGoingProceeding = "Don't know";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createProceeding(onGoingProceeding));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    private Proceeding createProceeding(String onGoingProceeding) {
        return new Proceeding(onGoingProceeding, "", "", "", "",
            "", "", "", "", "", "", null);
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Proceeding proceeding) throws Exception {
        Map<String, Object> map = mapper.readValue(mapper.writeValueAsString(proceeding), new TypeReference<>() {
        });

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("proceeding", map))
            .build();

        return postMidEvent(caseDetails);
    }
}
