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
import uk.gov.hmcts.reform.fpl.model.AdditionalChild;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)

public class ChildSubmissionControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenFirstChildDobIsInFuture() throws Exception {
        Address address = new Address("", "", "", "", "", "", "");
        Child firstChild = new Child("", "2040-10-01", "", "", "", "", "", "", "", "", "", "", "", "", "", address);
        Child secondChild = new Child("", "1999-10-01", "", "", "", "", "", "", "", "", "", "", "", "", "", address);
        AdditionalChild additionalChild = new AdditionalChild(UUID.randomUUID(), secondChild);
        Children children = new Children(firstChild, Arrays.asList(additionalChild));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(children), new TypeReference<Map<String, Object>>() {
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
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("Date of birth cannot be in the future");
    }

    @Test
    void shouldReturnErrorWhenAdditionalChildDobIsInFuture() throws Exception {
        Address address = new Address("", "", "", "", "", "", "");
        Child firstChild = new Child("", "1999-10-01", "", "", "", "", "", "", "", "", "", "", "", "", "", address);
        Child secondChild = new Child("", "2040-10-01", "", "", "", "", "", "", "", "", "", "", "", "", "", address);
        AdditionalChild additionalChild = new AdditionalChild(UUID.randomUUID(), secondChild);
        Children children = new Children(firstChild, Arrays.asList(additionalChild));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(children), new TypeReference<Map<String, Object>>() {
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
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).contains("Date of birth cannot be in the future");
    }

    @Test
    void shouldReturnNoErrorsWhenAllDobsAreInPast() throws Exception {
        Address address = new Address("", "", "", "", "", "", "");
        Child value = new Child("", "1999-10-01", "", "", "", "", "", "", "", "", "", "", "", "", "", address);
        AdditionalChild additionalChild = new AdditionalChild(UUID.randomUUID(), value);
        Children children = new Children(value, Arrays.asList(additionalChild));

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.readValue(mapper.writeValueAsString(children), new TypeReference<Map<String, Object>>() {
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
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getErrors()).doesNotContain("Date of birth cannot be in the future");
    }

}
