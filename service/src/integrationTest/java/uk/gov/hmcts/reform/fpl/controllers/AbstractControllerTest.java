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

    public AbstractControllerTest(String eventName) {
        this.eventName = eventName;
    }

    protected final String userAuthToken = "Bearer token";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper mapper;

    protected AboutToStartOrSubmitCallbackResponse postAboutToStart(Map<String, Object> data, int expectedHttpStats) {
        return postEvent(String.format("/callback/%s/about-to-start", eventName), data, expectedHttpStats);
    }

    protected AboutToStartOrSubmitCallbackResponse postAboutToStart(Map<String, Object> data) {
        return postAboutToStart(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(Map<String, Object> data, int expectedHttpStats) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), data, expectedHttpStats);
    }

    protected AboutToStartOrSubmitCallbackResponse postMidEvent(Map<String, Object> data) {
        return postMidEvent(data, SC_OK);
    }

    protected AboutToStartOrSubmitCallbackResponse postSubmittedEvent(Map<String, Object> data, int expectedHttpStats) {
        return postEvent(String.format("/callback/%s/submitted", eventName), data, expectedHttpStats);
    }

    protected AboutToStartOrSubmitCallbackResponse postSubmittedEvent(Long id, Map<String, Object> data,
                                                                      int expectedHttpStats) {
        return postEvent(String.format("/callback/%s/submitted", eventName), id, data, expectedHttpStats);
    }

    protected AboutToStartOrSubmitCallbackResponse postSubmittedEvent(Map<String, Object> data) {
        return postSubmittedEvent(data, SC_OK);
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, Long id, Map<String, Object> data,
                                                           int expectedHttpStats) {
        try {
            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().id(id).data(data).build()).build();

            MvcResult response = mockMvc
                .perform(post(path)
                    .header("authorization", userAuthToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                .andExpect(status().is(expectedHttpStats))
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
