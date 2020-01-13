package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

abstract class AbstractControllerTest {

    final String userAuthToken = "Bearer token";
    final String userId = "1";
    final String serviceAuthToken = "Bearer service-token";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    private String eventName;

    AbstractControllerTest(String eventName) {
        this.eventName = eventName;
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-start", eventName), data, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(byte[] data) {
        return postAboutToStartEvent(data, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postAboutToStartEvent(toBytes(callbackRequest), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CallbackRequest callbackRequest) {
        return postAboutToStartEvent(callbackRequest, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails, int expectedStatus) {
        return postAboutToStartEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails) {
        return postAboutToStartEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(String filename, int expectedStatus) {
        return postAboutToStartEvent(readBytes(filename), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(String filename) {
        return postAboutToStartEvent(filename, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), data, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data) {
        return postMidEvent(data, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest) {
        return postMidEvent(callbackRequest, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus) {
        return postMidEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails) {
        return postMidEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, int expectedStatus) {
        return postMidEvent(readBytes(filename), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(String filename) {
        return postMidEvent(filename, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-submit", eventName), data, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data) {
        return postAboutToSubmitEvent(data, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postAboutToSubmitEvent(toBytes(callbackRequest), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest) {
        return postAboutToSubmitEvent(callbackRequest, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails, int expectedStatus) {
        return postAboutToSubmitEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails) {
        return postAboutToSubmitEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename, int expectedStatus) {
        return postAboutToSubmitEvent(readBytes(filename), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename) {
        return postAboutToSubmitEvent(readBytes(filename), SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/submitted", eventName), data, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(byte[] data) {
        return postSubmittedEvent(data, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postSubmittedEvent(toBytes(callbackRequest), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest) {
        return postSubmittedEvent(callbackRequest, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(CaseDetails caseDetails, int expectedStatus) {
        return postSubmittedEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(CaseDetails caseDetails) {
        return postSubmittedEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(String filename, int expectedStatus) {
        return postSubmittedEvent(readBytes(filename), expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postSubmittedEvent(String filename) {
        return postSubmittedEvent(filename, SC_OK);
    }

    MvcResult postToUrl(String urlPath) throws Exception {
        return mockMvc
            .perform(post(String.format("%s/%s", eventName, urlPath))
                .header("authorization", userAuthToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, byte[] data, int expectedStatus) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", userAuthToken)
                    .header("user-id", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(data))
                .andExpect(status().is(expectedStatus))
                .andReturn();

            byte[] responseBody = response.getResponse().getContentAsByteArray();

            if (responseBody.length > 0) {
                return mapper.readValue(responseBody, AboutToStartOrSubmitCallbackResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toBytes(Object o) {
        try {
            return mapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CallbackRequest toCallbackRequest(CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
