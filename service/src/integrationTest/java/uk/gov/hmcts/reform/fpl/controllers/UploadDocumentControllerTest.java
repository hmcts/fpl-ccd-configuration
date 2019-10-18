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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDocumentControllerTest {

    private static final String CONTROLLER_ROOT = "/callback/upload-documents";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldSetDisplayCourtBundleToNoWhenCaseIsOpen() throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .state("Open")
                .data(ImmutableMap.of())
                .build())
            .build();

        MvcResult mvcResult = makeRequest(CONTROLLER_ROOT + "/about-to-start", callbackRequest);

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(mvcResult.getResponse()
            .getContentAsString(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(response.getData().containsKey("displayCourtBundle")).isTrue();
        String displayCourtBundle = (String) response.getData().get("displayCourtBundle");
        assertThat(displayCourtBundle).isEqualTo("No");
    }

    @Test
    void shouldSetDisplayCourtBundleToYesWhenCaseIsNotOpen() throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .state("Submitted")
                .data(ImmutableMap.of())
                .build())
            .build();

        MvcResult mvcResult = makeRequest(CONTROLLER_ROOT + "/about-to-start", callbackRequest);

        AboutToStartOrSubmitCallbackResponse response = objectMapper.readValue(mvcResult.getResponse()
            .getContentAsString(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(response.getData().containsKey("displayCourtBundle")).isTrue();
        String displayCourtBundle = (String) response.getData().get("displayCourtBundle");
        assertThat(displayCourtBundle).isEqualTo("Yes");
    }


    private MvcResult makeRequest(String endpoint, CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }


}
