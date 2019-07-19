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
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.MigratedChildren;
import uk.gov.hmcts.reform.fpl.model.OldChild;
import uk.gov.hmcts.reform.fpl.model.OldChildren;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildSubmissionControllerMidEventTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenFirstChildDateOfBirthIsInFuture() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime tomorrow = today.plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new OldChildren(createChild(tomorrow), createChild(today))
        );
        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorWhenAdditionalChildDateOfBirthIsInFuture() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime tomorrow = today.plusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new OldChildren(createChild(today), createChild(tomorrow))
        );
        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenAllDatesOfBirthAreTodayOrInPast() throws Exception {
        ZonedDateTime today = ZonedDateTime.now();
        ZonedDateTime yesterday = today.minusDays(1);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            new OldChildren(createChild(today), createChild(yesterday))
        );
        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForNewChildWhenFutureDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "123",
                            "value", MigratedChildren.builder()
                                .party(ChildParty.builder()
                                    .dateOfBirth(Date.from(ZonedDateTime.now().plusDays(1).toInstant()))
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
    void shouldReturnDateOfBirthErrorsForNewChildrenWhenThereIsMultipleChildren() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", MigratedChildren.builder()
                                .party(ChildParty.builder()
                                    .dateOfBirth(Date.from(ZonedDateTime.now().plusDays(1).toInstant()))
                                    .build())
                                .build()
                        ),
                        ImmutableMap.of(
                            "id", "",
                            "value", MigratedChildren.builder()
                                .party(ChildParty.builder()
                                    .dateOfBirth(Date.from(ZonedDateTime.now().plusDays(1).toInstant()))
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
    void shouldReturnNoDateOfBirthErrorsForNewChildrenWhenValidDateOfBirth() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        ImmutableMap.of(
                            "id", "",
                            "value", MigratedChildren.builder()
                                .party(ChildParty.builder()
                                    .dateOfBirth(Date.from(ZonedDateTime.now().minusDays(1).toInstant()))
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

    private OldChild createChild(ZonedDateTime dateOfBirth) {
        return OldChild.builder()
            .childDOB(Date.from(dateOfBirth.toInstant()))
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(OldChildren children) throws Exception {
        HashMap<String, Object> map = MAPPER.readValue(MAPPER.writeValueAsString(children),
            new TypeReference<Map<String, Object>>() {
            });

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder().put("children", map).build())
                .build())
            .build();

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
