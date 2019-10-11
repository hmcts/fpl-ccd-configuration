package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(OtherProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)

public class OtherProceedingsControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String ERROR_MESSAGE = "You must say if there are any other proceedings relevant to this case";

    @Autowired
    private MockMvc mockMvc;

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
            "", "", "", "", "", "");
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Proceeding proceeding) throws Exception {
        Map<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(proceeding), new TypeReference<>() {
        });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("proceeding", map).build())
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/enter-other-proceedings/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
