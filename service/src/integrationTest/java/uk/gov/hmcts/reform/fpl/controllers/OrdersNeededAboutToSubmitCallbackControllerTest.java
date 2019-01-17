package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class OrdersNeededAboutToSubmitCallbackControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAddEPOReasoningShowValueToCaseDataWhenCallbackContainsEPO() throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/orders-needed/about-to-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/case.json")))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData().get("EPO_REASONING_SHOW").toString()).isEqualTo("[SHOW_FIELD]");
    }

    @Test
    void shouldRemoveGroundsForEPODataWhenEPOIsUnselected() throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/orders-needed/about-to-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("fixtures/caseDataWithBeforeDetails.json")))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData().get("groundsForEPO")).isEqualTo(null);
    }
}
