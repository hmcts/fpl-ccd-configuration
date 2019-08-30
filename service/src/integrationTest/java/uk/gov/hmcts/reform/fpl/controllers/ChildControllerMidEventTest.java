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
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerMidEventTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDateOfBirthErrorWhenFutureDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(ZonedDateTime.now().plusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorhenThereIsMultipleChildren() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        createChildrenElement(ZonedDateTime.now().plusDays(1)),
                        createChildrenElement(ZonedDateTime.now().plusDays(1))
                    )))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorWhenValidDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(ZonedDateTime.now().minusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsWhenCaseDataIsEmpty() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private Map<String, Object> createChildrenElement(ZonedDateTime dateOfBirth) {
        return ImmutableMap.of(
            "id", "",
            "value", Child.builder()
                .party(ChildParty.builder()
                    .dateOfBirth(Date.from(dateOfBirth.toInstant()))
                    .build())
                .build());
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/enter-children/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andReturn();

        return MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
