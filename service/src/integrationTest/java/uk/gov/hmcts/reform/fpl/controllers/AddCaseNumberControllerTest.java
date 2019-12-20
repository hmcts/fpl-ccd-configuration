package uk.gov.hmcts.reform.fpl.controllers;

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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
public class AddCaseNumberControllerTest {
    private static final String FAMILY_MAN_CASE_NUMBER_KEY = "familyManCaseNumber";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void aboutToSubmitShouldReturnErrorWhenFamilymanCaseNumberNotAlphanumeric() throws Exception {
        CallbackRequest callbackRequest = buildCallbackRequest(randomAscii(10));

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(callbackRequest);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter a valid FamilyMan case number");
        assertThat(callbackResponse.getData()).doesNotContainKey(FAMILY_MAN_CASE_NUMBER_KEY);
    }

    @Test
    void aboutToSubmitShouldNotReturnErrorWhenFamilymanCaseNumberAlphanumeric() throws Exception {
        final String expectedFamilymanCaseNumber = randomAlphabetic(10);
        CallbackRequest callbackRequest = buildCallbackRequest(expectedFamilymanCaseNumber);

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(callbackRequest);

        assertThat(callbackResponse.getErrors()).isNull();
        assertThat(callbackResponse.getData()).containsKey(FAMILY_MAN_CASE_NUMBER_KEY);
        assertThat(callbackResponse.getData().get(FAMILY_MAN_CASE_NUMBER_KEY)).isEqualTo(expectedFamilymanCaseNumber);
    }

    private CallbackRequest buildCallbackRequest(final String familyManCaseNumber) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(FAMILY_MAN_CASE_NUMBER_KEY, familyManCaseNumber))
                .build())
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse makeRequest(final CallbackRequest request)
        throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/add-case-number/about-to-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper
            .readValue(response.getResponse().getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}
