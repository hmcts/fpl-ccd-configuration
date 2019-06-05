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
import uk.gov.hmcts.reform.fpl.model.Applicant;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
public class ApplicantControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenPbaNumberIsNotSevenDigits() throws Exception {
        String pbaNumber = "123";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createApplicant(pbaNumber));

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenPbaNumberStartsWithpbaAndIsSevenDigits() throws Exception {
        String pbaNumber = "pba1234567";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createApplicant(pbaNumber));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
        String actualPbaNumber = extractPbaNumberFromApplicant(callbackResponse);
        assertThat(actualPbaNumber).isEqualTo("PBA1234567");
    }

    @Test
    void shouldReturnNoErrorsWhenPbaNumberIsSevenDigits() throws Exception {
        String pbaNumber = "1234567";

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createApplicant(pbaNumber));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
        String actualPbaNumber = extractPbaNumberFromApplicant(callbackResponse);
        assertThat(actualPbaNumber).isEqualTo("PBA1234567");
    }

    @Test
    void shouldReturnNoErrorsWhenPbaNumberStartsWithPBAAndIsSevenDigits() throws Exception {
        String pbaNumber = "PBA1234567";
        
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createApplicant(pbaNumber));
        
        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
        String actualPbaNumber = extractPbaNumberFromApplicant(callbackResponse);
        assertThat(actualPbaNumber).isEqualTo("PBA1234567");
    }

    @Test
    void shouldReturnNoErrorsWhenPbaNumberIsNotEntered() throws Exception {
        String pbaNumber = null;

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(createApplicant(pbaNumber));

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
        String actualPbaNumber = extractPbaNumberFromApplicant(callbackResponse);
        assertThat(actualPbaNumber).isNull();
    }

    @SuppressWarnings("unchecked")
    private String extractPbaNumberFromApplicant(AboutToStartOrSubmitCallbackResponse callbackResponse) {
        Map<String, Object> map = callbackResponse.getData();
        Map<String, Object> applicantMap = (Map<String, Object>) map.get("applicant");
        String actualPbaNumber = (String) applicantMap.get("pbaNumber");
        return actualPbaNumber;
    }

    private Applicant createApplicant(String pbaNumber) {
        return new Applicant("","", "", "", null,
            "", "", "", pbaNumber);
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Applicant applicant) throws Exception {
        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(applicant),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("applicant", map).build())
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/enter-applicant/mid-event")
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
