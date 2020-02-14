package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerSubmittedTest extends AbstractControllerTest {

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private LDClient ldClient;

    CaseSubmissionControllerSubmittedTest() {
        super("case-submission");
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithNoData() {
        postSubmittedEvent(new byte[]{}, SC_BAD_REQUEST);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithMalformedData() {
        postSubmittedEvent("Mock".getBytes(), SC_BAD_REQUEST);
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValues() throws Exception {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");
        Map<String, Object> expectedHmctsParameters = ImmutableMap.<String, Object>builder()
            .put("court", "Family Court")
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        Map<String, Object> expectedCafcassParameters = ImmutableMap.<String, Object>builder()
            .put("cafcass", "cafcass")
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(ldClient.boolVariation(anyString(), any(), anyBoolean())).willReturn(false);

        postSubmittedEvent("core-case-data-store-api/callback-request.json");

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@family-court.com"), eq(expectedHmctsParameters), eq("12345")
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE), eq("cafcass@cafcass.com"), eq(expectedCafcassParameters), eq("12345")
        );

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@ctsc.com"), eq(expectedHmctsParameters), eq("12345")
        );
    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscFeatureIsEnabled() throws Exception {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");
        Map<String, Object> expectedHmctsParameters = ImmutableMap.<String, Object>builder()
            .put("court", "Family Court")
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(ldClient.boolVariation(anyString(), any(), anyBoolean())).willReturn(true);

        postSubmittedEvent("core-case-data-store-api/callback-request.json");

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@family-court.com"), eq(expectedHmctsParameters), eq("12345")
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@ctsc.com"), eq(expectedHmctsParameters), eq("12345")
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
            .put("ordersAndDirections", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("urgentHearing", "No")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        Map<String, Object> expectedCafcassParameters = ImmutableMap.<String, Object>builder()
            .put("cafcass", "cafcass")
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put("ordersAndDirections", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("urgentHearing", "No")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(ldClient.boolVariation(anyString(), any(), anyBoolean())).willReturn(false);

        postSubmittedEvent(request);

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@family-court.com"), eq(expectedHmctsParameters), eq("12345")
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE), eq("cafcass@cafcass.com"), eq(expectedCafcassParameters), eq("12345")
        );

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq("admin@ctsc.com"), eq(expectedHmctsParameters), eq("12345")
        );
    }
}
