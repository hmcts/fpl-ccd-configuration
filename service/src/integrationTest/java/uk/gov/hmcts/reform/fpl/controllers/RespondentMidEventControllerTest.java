package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.fpl.model.PartyExtended;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentMidEventControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenFirstRespondentDateOfBirthIsInFuture() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime tomorrow = today.plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new Respondents(createRespondent(tomorrow), createRespondent(today))
        );
        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenAdditionalRespondentDateOfBirthIsInFuture() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime tomorrow = today.plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new Respondents(createRespondent(today), createRespondent(tomorrow))
        );
        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenAllDatesOfBirthAreTodayOrInPast() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime yesterday = today.minusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new Respondents(createRespondent(today), createRespondent(yesterday))
        );
        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenThereIsNewRespondentAndNoOldRespondent() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", MigratedRespondent.builder()
                                .party(PartyExtended.builder()
                                    .dateOfBirth(Date.from(ZonedDateTime.now().plusDays(1).toInstant()))
                                    .build())
                                .build()
                        )
                    )
                ))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/enter-respondents/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    private Respondent createRespondent(ZonedDateTime dateOfBirth) {
        return new Respondent(null, Date.from(dateOfBirth.toInstant()), null, null, null,
            null, null, null, null);
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Respondents respondents) throws Exception {
        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(respondents),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("respondents", map).build())
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/enter-respondents/mid-event")
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
