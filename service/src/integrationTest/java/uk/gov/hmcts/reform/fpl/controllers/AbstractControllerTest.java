package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

abstract class AbstractControllerTest {

    static final String USER_AUTH_TOKEN = "Bearer token";
    static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    static final String USER_ID = "1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CaseConverter caseConverter;

    @Autowired
    private Time time;

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

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseData caseData) {
        return postAboutToStartEvent(asCaseDetails(caseData), SC_OK);
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

    AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, int expectedStatus, String additionalPath) {
        return postEvent(String.format("/callback/%s/%s/mid-event", eventName, additionalPath), data, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, String additionalPath) {
        return postMidEvent(data, SC_OK, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus,
                                                      String additionalPath) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, String additionalPath) {
        return postMidEvent(callbackRequest, SC_OK, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus,
                                                      String additionalPath) {
        return postMidEvent(toCallbackRequest(caseDetails), expectedStatus, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseData caseData, String additionalPath) {
        return postMidEvent(asCaseDetails(caseData), SC_OK, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, String additionalPath) {
        return postMidEvent(caseDetails, SC_OK, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, int expectedStatus, String additionalPath) {
        return postMidEvent(readBytes(filename), expectedStatus, additionalPath);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, String additionalPath) {
        return postMidEvent(filename, SC_OK, additionalPath);
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

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseData caseData) {
        return postAboutToSubmitEvent(asCaseDetails(caseData), SC_OK);
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

    SubmittedCallbackResponse postSubmittedEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/submitted", eventName), data, expectedStatus,
            SubmittedCallbackResponse.class);
    }

    SubmittedCallbackResponse postSubmittedEvent(byte[] data) {
        return postSubmittedEvent(data, SC_OK);
    }

    SubmittedCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postSubmittedEvent(toBytes(callbackRequest), expectedStatus);
    }

    SubmittedCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest) {
        return postSubmittedEvent(callbackRequest, SC_OK);
    }

    SubmittedCallbackResponse postSubmittedEvent(CaseDetails caseDetails, int expectedStatus) {
        return postSubmittedEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    SubmittedCallbackResponse postSubmittedEvent(CaseDetails caseDetails) {
        return postSubmittedEvent(caseDetails, SC_OK);
    }

    SubmittedCallbackResponse postSubmittedEvent(String filename, int expectedStatus) {
        return postSubmittedEvent(readBytes(filename), expectedStatus);
    }

    SubmittedCallbackResponse postSubmittedEvent(String filename) {
        return postSubmittedEvent(filename, SC_OK);
    }

    CaseData extractCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return mapper.convertValue(response.getData(), CaseData.class);
    }

    CaseDetails asCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(mapper.convertValue(caseData, new TypeReference<>() {}))
            .build();
    }

    CallbackRequest toCallBackRequest(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    LocalDateTime now() {
        return time.now();
    }

    LocalDate dateNow() {
        return time.now().toLocalDate();
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, byte[] data, int expectedStatus) {
        return postEvent(path, data, expectedStatus, AboutToStartOrSubmitCallbackResponse.class);
    }

    private <T> T postEvent(String path, byte[] data, int expectedStatus, Class<T> responseType) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", USER_AUTH_TOKEN)
                    .header("user-id", USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(data))
                .andExpect(status().is(expectedStatus))
                .andReturn();

            byte[] responseBody = response.getResponse().getContentAsByteArray();

            if (responseBody.length > 0) {
                return mapper.readValue(responseBody, responseType);
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
