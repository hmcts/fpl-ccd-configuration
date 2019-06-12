package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(SocialWorkOtherSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
public class SocialWorkOtherControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "10";
    private static final String CASE_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private CaseAccessApi caseAccessApi;

    @Test
    void shouldReturnWithErrorsIfDocumentTitleIsNotPopulated() throws Exception {
        CallbackRequest request = createCallbackRequest("");

        MvcResult response = performResponseCallBack(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("You must give additional document 1 a name.");
    }

    @Test
    void shouldReturnWithErrorsIfMultipleDocumentTitlesHaveNotBeenProvided() throws Exception {
        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("documents_socialWorkOther", ImmutableList.builder()
                    .add(ImmutableMap.builder()
                        .put("id", "12345")
                        .put("value", ImmutableMap.builder()
                            .put("documentTitle", "Document title one")
                            .build())
                        .build())
                    .add(ImmutableMap.builder()
                        .put("id", "12346")
                        .put("value", ImmutableMap.builder()
                            .put("documentTitle", "Document title two")
                            .build())
                        .build())
                    .build())
                .build()).build())
            .build();

        MvcResult response = performResponseCallBack(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("You must give additional document 1 a name. You must give additional document 2 a name.");
    }

    @Test
    void shouldReturnWithNoErrorsIfDocumentTitleIsPopulated() throws Exception {
        CallbackRequest request = createCallbackRequest("123");

        MvcResult response = performResponseCallBack(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private CallbackRequest createCallbackRequest(String documentTitle) {
        return CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("documents_socialWorkOther", ImmutableList.builder()
                    .add(ImmutableMap.builder()
                        .put("id", "12345")
                        .put("value", ImmutableMap.builder()
                            .put("documentTitle", documentTitle)
                            .build())
                        .build())
                    .build())
                .build()).build())
            .build();
    }

    private MvcResult performResponseCallBack(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/enter-social-work-other/mid-event")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }
}
