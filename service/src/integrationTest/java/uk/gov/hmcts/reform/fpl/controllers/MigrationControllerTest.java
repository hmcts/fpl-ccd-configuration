
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
import uk.gov.hmcts.reform.fpl.model.OldChildren;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrationControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRemoveApplicantFromDataStructure() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            ImmutableMap.of("children", OldChildren.builder().build()));

        assertThat(callbackResponse.getData()).doesNotContainKey("children");
    }

    @Test
    void shouldContinueWithoutErrorsWhenApplicantIsNotPresentInDataStructure() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(ImmutableMap.of("data", ""));

        assertThat(callbackResponse.getData()).doesNotContainKey("children1");
    }

    @Test
    void shouldOnlyRemoveApplicantWhilstRetainingApplicantsStructure() throws Exception {
        List<Element<Child>> children = ImmutableList.of(Element.<Child>builder()
            .value(Child.builder().build())
            .build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
            ImmutableMap.of(
                "children", OldChildren.builder().build(),
                "children1", children
            ));

        assertThat(callbackResponse.getData()).containsOnlyKeys("children1");
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(Map<String, Object> map) throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.copyOf(map))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/migration/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
