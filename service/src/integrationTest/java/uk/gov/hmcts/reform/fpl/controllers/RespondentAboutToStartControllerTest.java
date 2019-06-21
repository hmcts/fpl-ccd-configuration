package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentAboutToStartControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Test
    void shouldAddRespondentMigratedYesValueToDataWhenRespondents1IsPresent() throws Exception {
        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("respondents1", "populated")
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getData()).containsEntry("respondentsMigrated", "Yes");
    }

    @Test
    void shouldAddRespondentMigratedNoValueToDataWhenRespondents1IsNotPresent() throws Exception {
        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("data", "some data")
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getData()).containsEntry("respondentsMigrated", "No");
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/enter-respondents/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
