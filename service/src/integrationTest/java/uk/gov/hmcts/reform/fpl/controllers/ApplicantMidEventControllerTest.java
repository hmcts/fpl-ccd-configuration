package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
public class ApplicantMidEventControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorsWhenThereIsNewApplicantAndNoPbaNumber() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber("")
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenThereIsNewApplicantAndNoPbaNumberIsNull() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber(null)
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenThereIsNewApplicantAndPbaNumberIsNotSevenDigits() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber("123")
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberIsSevenDigits() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber("1234567")
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberWithpbaAndSevenDigits() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber("pba1234567")
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberWithPBAAndSevenDigits() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "applicants", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Applicant.builder()
                                .party(ApplicantParty.builder()
                                    .pbaNumber("PBA1234567")
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = getMvcResult(request);

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        Assertions.assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }



    private MvcResult getMvcResult(CallbackRequest request) throws Exception {
        return mockMvc
                .perform(post("/callback/enter-applicant/mid-event")
                    .header("authorization", AUTH_TOKEN)
                    .header("user-id", USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
    }
}
