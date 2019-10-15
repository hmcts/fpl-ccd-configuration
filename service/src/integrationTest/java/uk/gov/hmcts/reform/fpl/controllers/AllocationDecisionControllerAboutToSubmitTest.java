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
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocationDecisionControllerAboutToSubmitTest {
    private static final String AUTH_TOKEN = "Bearer token";

    @Autowired
    private  ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPopulateAllocationDecisionWhenSubmitting() throws Exception {
        Allocation allocationDecision = createAllocation("Lay justices", "Reason");

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("allocationDecision", allocationDecision)
                .build()).build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/allocation-decision/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData()).containsKey("allocationDecision");
    }

    private AboutToStartOrSubmitCallbackResponse callbackResponse(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/allocation-decision/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        return callbackResponse;
    }

    private Allocation createAllocation(String proposal, String judgeLevelRadio) throws Exception {
        Allocation allocationDecision = Allocation.builder()
            .proposal(proposal)
            .judgeLevelRadio(judgeLevelRadio)
            .build();
        return allocationDecision;
    }
}
