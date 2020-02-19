package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerSubmittedTest extends AbstractControllerTest {
    private static final String FAMILY_COURT = "Family Court";
    private static final String CAFCASS_COURT = "cafcass";

    @MockBean
    private NotificationClient notificationClient;

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
        Map<String, Object> completeHmctsParameters = getCompleteParameters()
            .put("court", FAMILY_COURT)
            .build();

        Map<String, Object> completeCafcassParameters = getCompleteParameters()
            .put("cafcass", CAFCASS_COURT)
            .build();

        postSubmittedEvent("core-case-data-store-api/callback-request.json");

        verify(notificationClient).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("admin@family-court.com"),
            eq(completeHmctsParameters),
            eq("12345")
        );

        verify(notificationClient).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE),
            eq("cafcass@cafcass.com"),
            eq(completeCafcassParameters),
            eq("12345")
        );

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            eq(completeHmctsParameters),
            eq("12345")
        );
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() throws Exception {
        CaseDetails caseDetails = enabledSendToCtscOnCaseDetails(NO);

        Map<String, Object> expectedHmctsParameters = getInCompleteParameters()
            .put("court", FAMILY_COURT)
            .build();

        Map<String, Object> expectedCafcassParameters = getInCompleteParameters()
            .put("cafcass", CAFCASS_COURT)
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("admin@family-court.com"),
            eq(expectedHmctsParameters),
            eq("12345")
        );

        verify(notificationClient).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE),
            eq("cafcass@cafcass.com"),
            eq(expectedCafcassParameters),
            eq("12345")
        );

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            eq(expectedHmctsParameters),
            eq("12345")
        );
    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() throws Exception {
        CaseDetails caseDetails = enabledSendToCtscOnCaseDetails(YES);
        Map<String, Object> expectedHmctsParameters = getInCompleteParameters()
            .put("court", FAMILY_COURT)
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("admin@family-court.com"),
            eq(expectedHmctsParameters),
            eq("12345")
        );

        verify(notificationClient).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            eq(expectedHmctsParameters),
            eq("12345")
        );
    }

    private CaseDetails enabledSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of(
                "caseLocalAuthority", "example",
                "sendToCtsc", enableCtsc.getValue()
            )).build();
    }

    private ImmutableMap.Builder<String, Object> getCompleteParameters() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        return ImmutableMap.<String, Object>builder()
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
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
    }

    private ImmutableMap.Builder<String, Object> getInCompleteParameters() {
        return ImmutableMap.<String, Object>builder()
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
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
    }
}
