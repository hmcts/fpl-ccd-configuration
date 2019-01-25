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
import uk.gov.hmcts.reform.fpl.model.AdditionalRespondent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Respondents;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)

public class RespondentSubmissionControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenFirstRespondentDobIsInFuture() throws Exception {

        Date tomorrow = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(tomorrow);
        c.add(Calendar.DATE, 1);
        tomorrow = c.getTime();

        Address address = new Address("", "", "", "", "", "", "");
        Respondent respondent = new Respondent("", tomorrow, "", "", "", "",
            "", "", address);
        AdditionalRespondent additionalRespondent = new AdditionalRespondent(UUID.randomUUID(), respondent);
        Respondents respondents = new Respondents(respondent, Arrays.asList(additionalRespondent));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(respondents),
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("Date of birth cannot be in the future");
    }

    @Test
    void shouldReturnNoErrorsWhenFirstRespondentDobIsInPast() throws Exception {

        Address address = new Address("", "", "", "", "", "", "");
        Respondent firstRespondent = new Respondent("", new Date(), "", "", "",
            "", "", "", address);
        Respondent secondRespondent = new Respondent("", new Date(), "", "", "",
            "", "", "", address);
        AdditionalRespondent additionalRespondent = new AdditionalRespondent(UUID.randomUUID(), secondRespondent);
        Respondents respondents = new Respondents(firstRespondent, Arrays.asList(additionalRespondent));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(respondents),
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).doesNotContain("Date of birth cannot be in the future");
    }

    @Test
    void shouldReturnErrorsWhenAdditionalRespondentDobIsInFuture() throws Exception {

        Date tomorrow = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(tomorrow);
        c.add(Calendar.DATE, 1);
        tomorrow = c.getTime();

        Address address = new Address("", "", "", "", "", "", "");
        Respondent firstRespondent = new Respondent("", new Date(), "", "", "",
            "", "", "", address);
        Respondent secondRespondent = new Respondent("", tomorrow, "", "", "",
            "", "", "", address);
        AdditionalRespondent additionalRespondent = new AdditionalRespondent(UUID.randomUUID(), secondRespondent);
        Respondents respondents = new Respondents(firstRespondent, Arrays.asList(additionalRespondent));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(respondents),
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("Date of birth cannot be in the future");
    }

}
