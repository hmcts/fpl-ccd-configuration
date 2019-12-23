package uk.gov.hmcts.reform.fpl.controllers;

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

abstract class AbstractControllerTest {

    private String eventName;

    final String userAuthToken = "Bearer token";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    AbstractControllerTest(String eventName) {
        this.eventName = eventName;
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails,
                                                               int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-start", eventName), caseDetails, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToStartEvent(CaseDetails caseDetails) {
        return postAboutToStartEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails, int expectedStatus) {
        return postEvent(String.format("/callback/%s/mid-event", eventName), caseDetails, expectedStatus);
    }

    AboutToStartOrSubmitCallbackResponse postMidEvent(CaseDetails caseDetails) {
        return postMidEvent(caseDetails, SC_OK);
    }

    AboutToStartOrSubmitCallbackResponse postAboutToSubmitEvent(CaseDetails caseDetails,
                                                                int expectedStatus) {
        return postEvent(String.format("/callback/%s/about-to-submit", eventName), caseDetails, expectedStatus);
    }

    private AboutToStartOrSubmitCallbackResponse postEvent(String path, CaseDetails caseDetails, int expectedStatus) {
        try {
            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails).build();

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
}
