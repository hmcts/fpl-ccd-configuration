package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToStartTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void mockUserNameRetrieval() {
        given(userDetailsService.getUserName(AUTH_TOKEN)).willReturn("Emma Taylor");
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = performRequest(CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                        .data(ImmutableMap.<String, Object>builder()
                                .put("caseName", "title")
                                .build()).build())
                .build());

        assertThat(callbackResponse.getData())
                .containsEntry("caseName", "title")
                .containsEntry("submissionConsentLabel",
                        "I, Emma Taylor, believe that the facts stated in this application are true.");
    }

    @Nested
    class LocalAuthorityValidation {
        @Test
        void shouldReturnErrorWhenCaseBelongsToSmokeTestLocalAuthority() throws Exception {
            AboutToStartOrSubmitCallbackResponse callbackResponse = performRequest(prepareCaseBelongingTo("FPLA"));

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "FPLA");
            assertThat(callbackResponse.getErrors()).contains("Test local authority cannot submit cases");
        }

        @Test
        void shouldReturnNoErrorWhenCaseBelongsToRegularLocalAuthority() throws Exception {
            AboutToStartOrSubmitCallbackResponse callbackResponse = performRequest(prepareCaseBelongingTo("SA"));

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "SA");
            assertThat(callbackResponse.getErrors()).isEmpty();
        }

        private CallbackRequest prepareCaseBelongingTo(String localAuthority) {
            return CallbackRequest.builder()
                    .caseDetails(CaseDetails.builder()
                            .data(ImmutableMap.<String, Object>builder()
                                    .put("caseLocalAuthority", localAuthority)
                                    .build()).build())
                    .build();
        }
    }

    private AboutToStartOrSubmitCallbackResponse performRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
                .perform(post("/callback/case-submission/about-to-start")
                        .header("authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return MAPPER.readValue(response.getResponse()
                .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
