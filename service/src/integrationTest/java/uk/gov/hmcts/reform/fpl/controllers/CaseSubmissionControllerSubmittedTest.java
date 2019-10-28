package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerSubmittedTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String ORDERS_AND_DIRECTIONS = "ordersAndDirections";
    private static final String CAFCASS = "cafcass";
    private static final String SUBMITTED_CALLBACK_URL = "/callback/case-submission/submitted";
    private static final String AUTHORIZATION = "authorization";
    private static final String USER_ID_STR = "user-id";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, Object> expectedSimilarParameters = ImmutableMap.<String, Object>builder()
        .put("localAuthority", "Example Local Authority")
        .put("dataPresent", "Yes")
        .put("fullStop", "No")
        .put("timeFramePresent", "Yes")
        .put("timeFrameValue", "Same day")
        .put("reference", "12345")
        .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
        .build();

    @MockBean
    private NotificationClient notificationClient;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnsuccessfulResponseWithNoData() throws Exception {
        mockMvc
            .perform(post(SUBMITTED_CALLBACK_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .header(USER_ID_STR, USER_ID))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithMalformedData() throws Exception {
        mockMvc
            .perform(post(SUBMITTED_CALLBACK_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .header(USER_ID_STR, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("Mock"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValues() throws Exception {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");
        Map<String, Object> expectedHmctsParameters = ImmutableMap.<String, Object>builder()
            .putAll(expectedSimilarParameters)
            .put("court", "Family Court")
            .put(ORDERS_AND_DIRECTIONS, ordersAndDirections)
            .build();

        Map<String, Object> expectedCafcassParameters = ImmutableMap.<String, Object>builder()
            .put(CAFCASS, CAFCASS)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put(ORDERS_AND_DIRECTIONS, ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        mockMvc
            .perform(post(SUBMITTED_CALLBACK_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .header(USER_ID_STR, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/callback-request.json")))
            .andExpect(status().isOk());

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE.getTemplateId()), eq("admin@family-court.com"),
            eq(expectedHmctsParameters), eq("12345")
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE.getTemplateId()), eq("cafcass@cafcass.com"),
            eq(expectedCafcassParameters), eq("12345")
        );
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.<String, Object>builder()
                    .put("caseLocalAuthority", "example").build())
                .build())
            .build();

        Map<String, Object> expectedHmctsParameters = ImmutableMap.<String, Object>builder()
            .put("court", "Family Court")
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put(ORDERS_AND_DIRECTIONS, "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        Map<String, Object> expectedCafcassParameters = ImmutableMap.<String, Object>builder()
            .put(CAFCASS, CAFCASS)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put(ORDERS_AND_DIRECTIONS, "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        mockMvc
            .perform(post(SUBMITTED_CALLBACK_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .header(USER_ID_STR, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE.getTemplateId()),
            eq("admin@family-court.com"), eq(expectedHmctsParameters), eq("12345")
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE.getTemplateId()), eq("cafcass@cafcass.com"),
            eq(expectedCafcassParameters), eq("12345")
        );
    }
}
