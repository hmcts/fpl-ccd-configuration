package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentMidEventControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenFutureDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenThereIsMultipleRespondents() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .build())
                                .build()
                        ),
                        ImmutableMap.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().plusDays(1))
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .dateOfBirth(LocalDate.now().minusDays(1))
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/enter-respondents/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
