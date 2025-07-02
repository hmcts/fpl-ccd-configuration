package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.fpl.interceptors.LastGenuineUpdateTimeInterceptor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

public abstract class AbstractCallbackTest extends AbstractTest {

    @MockBean
    protected LastGenuineUpdateTimeInterceptor lastGenuineUpdateTimeInterceptor;

    @Autowired
    private MockMvc mockMvc;

    private final String eventName;
    private final String eventId;
    protected static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;

    protected AbstractCallbackTest(String eventName) {
        this.eventName = eventName;
        this.eventId = eventName;
    }

    protected AbstractCallbackTest(String eventName, String eventId) {
        this.eventName = eventName;
        this.eventId = eventId;
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

    protected AboutToStartOrSubmitCallbackResponse postMidEventWithUserRole(CallbackRequest callbackRequest,
                                                                            String... userRoles) {
        return postMidEvent(callbackRequest, SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, int expectedStatus, String... userRoles) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), data, expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data) {
        return postMidEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus,
                                                                String... userRoles) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus, userRoles);
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
                                                                String additionalPath, String... userRoles) {
        return postEvent(
            String.format("/callback/%s/%s/mid-event", eventName, additionalPath), data, expectedStatus, userRoles
        );
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(byte[] data, String additionalPath,
                                                                String... userRoles) {
        return postMidEvent(data, SC_OK, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest, int expectedStatus,
                                                                String additionalPath, String... userRoles) {
        return postMidEvent(toBytes(callbackRequest), expectedStatus, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest,
                                                                String additionalPath, String... userRoles) {
        return postMidEvent(callbackRequest, SC_OK, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus,
                                                                String additionalPath, String... userRoles) {
        return postMidEvent(toCallbackRequest(caseDetails), expectedStatus, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseData caseData) {
        return postMidEvent(asCaseDetails(caseData), SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseData caseData, String additionalPath,
                                                                String... userRoles) {
        return postMidEvent(asCaseDetails(caseData), SC_OK, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, String additionalPath,
                                                                String... userRoles) {
        return postMidEvent(caseDetails, SC_OK, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, int expectedStatus,
                                                                String additionalPath, String... userRoles) {
        return postMidEvent(readBytes(filename), expectedStatus, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(String filename, String additionalPath,
                                                                String... userRoles) {
        return postMidEvent(filename, SC_OK, additionalPath, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data, int expectedStatus,
                                                                          String... userRoles) {
        return postEvent(String.format("/callback/%s/about-to-submit", eventName), data, expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(byte[] data, String... userRoles) {
        return postAboutToSubmitEvent(data, SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest,
                                                                          int expectedStatus, String... userRoles) {
        return postAboutToSubmitEvent(toBytes(callbackRequest), expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CallbackRequest callbackRequest,
                                                                          String... userRoles) {
        return postAboutToSubmitEvent(callbackRequest, SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails, int expectedStatus,
                                                                          String... userRoles) {
        return postAboutToSubmitEvent(toCallbackRequest(caseDetails), expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseData caseData, String... userRoles) {
        return postAboutToSubmitEvent(asCaseDetails(caseData), SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails,
                                                                          String... userRoles) {
        return postAboutToSubmitEvent(caseDetails, SC_OK, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename, int expectedStatus,
                                                                          String... userRoles) {
        return postAboutToSubmitEvent(readBytes(filename), expectedStatus, userRoles);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(String filename, String... userRoles) {
        return postAboutToSubmitEvent(readBytes(filename), SC_OK, userRoles);
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

    protected CaseData extractCaseData(CallbackRequest callbackRequest) {
        return mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
    }

    protected CaseData extractCaseData(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }

    protected CallbackRequest toCallBackRequest(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected CallbackRequest toCallBackRequest(CaseData caseData, CaseData caseDataBefore) {
        return toCallBackRequest(asCaseDetails(caseData), asCaseDetails(caseDataBefore));
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> postMetadataCallback(String path, CallbackRequest callbackRequest) {
        return postEvent(path, toBytes(callbackRequest), SC_OK, Map.class);
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
            .eventId(eventId)
            .caseDetailsBefore(CaseDetails.builder().data(Map.of()).build())
            .build();
    }

    protected void givenFplService() {
        super.givenFplService();
        given(lastGenuineUpdateTimeInterceptor.supports(any(), any(), any())).willReturn(false);
        given(lastGenuineUpdateTimeInterceptor.supports(any(), any())).willReturn(false);
    }
}
