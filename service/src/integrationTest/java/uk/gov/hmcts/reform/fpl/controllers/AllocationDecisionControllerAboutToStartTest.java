package uk.gov.hmcts.reform.fpl.controllers;

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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocationDecisionControllerAboutToStartTest {

    private final AllocationDecisionController controller = new AllocationDecisionController();
    private static final String AUTH_TOKEN = "Bearer token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddYesToMissingAllocationDecision() throws Exception {

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("allocationProposal","allocation proposal present")
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackResponse(request);

        Map<String, Object> allocationDecision = (Map<String, Object>) response.getData().get("allocationDecision");
        assertThat(allocationDecision)
            .containsEntry("allocationProposalPresent", "Yes");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldAddNoToMissingAllocationDecision() throws Exception {

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackResponse(request);

        Map<String, Object> allocationDecision = (Map<String, Object>) response.getData().get("allocationDecision");
        assertThat(allocationDecision)
            .containsEntry("allocationProposalPresent", "No");
    }

    private AboutToStartOrSubmitCallbackResponse callbackResponse(CallbackRequest request) throws Exception {

        MvcResult response = mockMvc
            .perform(post("/callback/allocation-decision/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        return callbackResponse;
    }
}
