package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
public class ApplicantMidEventControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "pba1234567", "PBA1234567" })
    void shouldReturnNoErrorsWhenValidPbaNumber(String input) throws Exception {
        CallbackRequest request = getCallbackRequest(input);

        assertPbaNumberIsAsExpected(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", "\t", "\n", "123", "12345678"})
    void shouldReturnErrorsWhenThereIsInvalidPbaNumber(String input) throws Exception {
        CallbackRequest request = getCallbackRequest(input);

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberIsNull() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("applicants", ImmutableList.of(Element.builder()
                    .id(UUID.randomUUID())
                    .value(Applicant.builder()
                        .party(ApplicantParty.builder().build())
                        .build())
                    .build())))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(
            makeRequest(request).getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    private CallbackRequest getCallbackRequest(String input) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("applicants", ImmutableList.of(Element.builder()
                    .id(UUID.randomUUID())
                    .value(Applicant.builder()
                        .party(ApplicantParty.builder()
                            .pbaNumber(input)
                            .build())
                        .build())
                    .build())))
                .build())
            .build();
    }

    private MvcResult makeRequest(CallbackRequest request) throws Exception {
        return mockMvc
            .perform(post("/callback/enter-applicant/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();
    }

    private void assertPbaNumberIsAsExpected(CallbackRequest request) throws Exception {
        MvcResult response = makeRequest(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = objectMapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);

        CaseData caseData = objectMapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getApplicants().get(0).getValue().getParty().getPbaNumber()).isEqualTo("PBA1234567");
    }
}
