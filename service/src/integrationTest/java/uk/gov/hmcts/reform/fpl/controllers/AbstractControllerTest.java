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
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

public abstract class AbstractControllerTest {

    protected static final String USER_AUTH_TOKEN = "Bearer token";
    protected static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    protected static final String USER_ID = "1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    CaseConverter caseConverter;

    @Autowired
    private Time time;

    private final String eventName;

    protected AbstractControllerTest(String eventName) {
        this.eventName = eventName;
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(byte[] data, int expectedStatus,
                                                                         String... userRoles) {
        return postEvent(String.format("/callback/%s/about-to-start", eventName), data, expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(byte[] data) {
        return postAboutToStartEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CallbackRequest callbackRequest,
                                                                         int expectedStatus, String... userRoles) {
        return postAboutToStartEvent(toBytes(callbackRequest), expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CallbackRequest callbackRequest) {
        return postAboutToStartEvent(callbackRequest, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails, int expectedStatus,
                                                                         String... userRoles) {
        return postAboutToStartEvent(toCallbackRequest(caseDetails), expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails) {
        return postAboutToStartEvent(caseDetails, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(String filename, int expectedStatus) {
        return postAboutToStartEvent(readBytes(filename), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(String filename) {
        return postAboutToStartEvent(filename, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseData caseData, String... userRoles) {
        return postAboutToStartEvent(asCaseDetails(caseData), SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), data, expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data) {
        return postMidEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest) {
        return postMidEvent(callbackRequest, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus) {
        return postMidEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails) {
        return postMidEvent(caseDetails, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, int expectedStatus) {
        return postMidEvent(readBytes(filename), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename) {
        return postMidEvent(filename, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, int expectedStatus,
                                                                String additionalPath) {
        return postEvent(String.format("/callback/%s/%s/mid-event", eventName, additionalPath), data, expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, String additionalPath) {
        return postMidEvent(data, SC_OK, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus,
                                                                String additionalPath) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest,
                                                                String additionalPath) {
        return postMidEvent(callbackRequest, SC_OK, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus,
                                                                String additionalPath) {
        return postMidEvent(toCallbackRequest(caseDetails), expectedStatus, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseData caseData, String additionalPath) {
        return postMidEvent(asCaseDetails(caseData), SC_OK, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, String additionalPath) {
        return postMidEvent(caseDetails, SC_OK, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, int expectedStatus,
                                                                String additionalPath) {
        return postMidEvent(readBytes(filename), expectedStatus, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, String additionalPath) {
        return postMidEvent(filename, SC_OK, additionalPath);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-submit", eventName), data, expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data) {
        return postAboutToSubmitEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest,
                                                                          int expectedStatus) {
        return postAboutToSubmitEvent(toBytes(callbackRequest), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest) {
        return postAboutToSubmitEvent(callbackRequest, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails, int expectedStatus) {
        return postAboutToSubmitEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseData caseData) {
        return postAboutToSubmitEvent(asCaseDetails(caseData), SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails) {
        return postAboutToSubmitEvent(caseDetails, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename, int expectedStatus) {
        return postAboutToSubmitEvent(readBytes(filename), expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename) {
        return postAboutToSubmitEvent(readBytes(filename), SC_OK);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(byte[] data, int expectedStatus) {
        return postEvent(String.format("/callback/%s/submitted", eventName), data, expectedStatus,
            SubmittedCallbackResponse.class);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(byte[] data) {
        return postSubmittedEvent(data, SC_OK);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest, int expectedStatus) {
        return postSubmittedEvent(toBytes(callbackRequest), expectedStatus);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(CallbackRequest callbackRequest) {
        return postSubmittedEvent(callbackRequest, SC_OK);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(CaseDetails caseDetails, int expectedStatus) {
        return postSubmittedEvent(toCallbackRequest(caseDetails), expectedStatus);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(CaseDetails caseDetails) {
        return postSubmittedEvent(caseDetails, SC_OK);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(CaseData caseData) {
        return postSubmittedEvent(asCaseDetails(caseData), SC_OK);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(String filename, int expectedStatus) {
        return postSubmittedEvent(readBytes(filename), expectedStatus);
    }

    protected SubmittedCallbackResponse postSubmittedEvent(String filename) {
        return postSubmittedEvent(filename, SC_OK);
    }

    protected CaseData extractCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return mapper.convertValue(response.getData(), CaseData.class);
    }

    protected CaseDetails asCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(mapper.convertValue(caseData, MAP_TYPE))
            .build();
    }

    protected CallbackRequest toCallBackRequest(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected LocalDateTime now() {
        return time.now();
    }

    protected LocalDate dateNow() {
        return time.now().toLocalDate();
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, byte[] data, int expectedStatus,
                                                           String... userRoles) {
        return postEvent(path, data, expectedStatus, AboutToStartOrSubmitCallbackResponse.class, userRoles);
    }

    private <T> T postEvent(String path, byte[] data, int expectedStatus, Class<T> responseType, String... userRoles) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", USER_AUTH_TOKEN)
                    .header("user-id", USER_ID)
                    .header("user-roles", String.join(",", userRoles))
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
