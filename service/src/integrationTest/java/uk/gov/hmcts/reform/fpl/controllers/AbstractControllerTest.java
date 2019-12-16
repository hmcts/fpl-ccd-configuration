package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AbstractControllerTest {

    private String eventName;

    protected final String userAuthToken = "Bearer token";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper mapper;

    public AbstractControllerTest(String eventName) {
        this.eventName = eventName;
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(Map<String, Object> caseData,
                                                                         int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-start", eventName), caseData, expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(Map<String, Object> caseData) {
        return postAboutToStartEvent(caseData, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(Map<String, Object> caseData, int expectedStatus) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), caseData, expectedStatus);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(Map<String, Object> data) {
        return postMidEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(Long id, Map<String, Object> caseData,
                                                                          int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-submit", eventName), id, caseData, expectedStatus);
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, Long id, Map<String, Object> caseData,
                                                           int expectedStatus) {
        try {
            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().id(id).data(caseData).build()).build();

            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", userAuthToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus))
                .andReturn();

            return mapper.readValue(response.getResponse().getContentAsByteArray(),
                AboutToStartOrSubmitCallbackResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, Map<String, Object> data,
                                                           int expectedHttpStats) {
        return postEvent(path, RandomUtils.nextLong(), data, expectedHttpStats);
    }

}
