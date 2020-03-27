package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
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
    private static final Long CASE_REFERENCE = 12345L;
    private static final String HMCTS_ADMIN_EMAIL = "admin@family-court.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";

    @MockBean
    private LDClient ldClient;

    @MockBean
    private PaymentService paymentService;

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
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            completeHmctsParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            CAFCASS_EMAIL,
            completeCafcassParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            completeHmctsParameters,
            CASE_REFERENCE.toString());
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() throws Exception {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);

        Map<String, Object> expectedHmctsParameters = getInCompleteParameters()
            .put("court", FAMILY_COURT)
            .build();

        Map<String, Object> expectedCafcassParameters = getInCompleteParameters()
            .put("cafcass", CAFCASS_COURT)
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            expectedHmctsParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            CAFCASS_EMAIL,
            expectedCafcassParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_EMAIL,
            expectedHmctsParameters,
            CASE_REFERENCE.toString());
    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() throws Exception {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        Map<String, Object> expectedHmctsParameters = getInCompleteParameters()
            .put("court", FAMILY_COURT)
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            expectedHmctsParameters,
            CASE_REFERENCE.toString()
        );

        verify(notificationClient).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_EMAIL,
            expectedHmctsParameters,
            CASE_REFERENCE.toString()
        );
    }

    @Nested
    class MakePaymentForCaseOrders {

        @Test
        void shouldMakePaymentWhenFeatureToggleIsTrueAndAmountToPayWasDisplayed() {
            given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
            caseDetails.getData().put("displayAmountToPay", YES.getValue());

            postSubmittedEvent(caseDetails);

            verify(paymentService).makePaymentForCaseOrders(CASE_REFERENCE,
                mapper.convertValue(caseDetails.getData(), CaseData.class));
        }

        @Test
        void shouldNotMakePaymentWhenFeatureToggleIsFalse() {
            given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(false);
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
            caseDetails.getData().put("displayAmountToPay", YES.getValue());

            postSubmittedEvent(caseDetails);

            verify(paymentService, never()).makePaymentForCaseOrders(any(), any());
        }

        @Test
        void shouldNotMakePaymentWhenAmountToPayWasNotDisplayed() {
            given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
            caseDetails.getData().put("displayAmountToPay", NO.getValue());

            postSubmittedEvent(caseDetails);

            verify(paymentService, never()).makePaymentForCaseOrders(any(), any());
        }

        @AfterEach
        void resetInvocations() {
            reset(paymentService);
        }
    }

    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() throws NotificationClientException {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", YES.getValue());

        doThrow(new PaymentsApiException(1, "", new Throwable())).when(paymentService)
            .makePaymentForCaseOrders(any(), any());

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C110a"),
            "12345");

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            "12345");
    }

    @Test
    void shouldNotSendFailedPaymentNotificationWhenDisplayAmountToPayNotSet() throws NotificationClientException {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);

        doThrow(new PaymentsApiException(1, "", new Throwable())).when(paymentService)
            .makePaymentForCaseOrders(any(), any());

        postSubmittedEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C110a"),
            "12345");

        verify(notificationClient, never()).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            "12345");
    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() throws NotificationClientException {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());

        postSubmittedEvent(caseDetails);

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C110a"),
            "12345");

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            "12345");
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C110a",
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345");
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(new HashMap<>(Map.of(
                "caseLocalAuthority", "example",
                "sendToCtsc", enableCtsc.getValue()
            ))).build();
    }

    private ImmutableMap.Builder<String, Object> getCompleteParameters() {
        List<String> ordersAndDirections = List.of("Emergency protection order", "Contact with any named person");

        return ImmutableMap.<String, Object>builder()
            .putAll(getCommonParameters())
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith");
    }

    private ImmutableMap.Builder<String, Object> getInCompleteParameters() {
        return ImmutableMap.<String, Object>builder()
            .putAll(getCommonParameters())
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put("ordersAndDirections", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("urgentHearing", "No")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "");
    }

    private Map<String, Object> getCommonParameters() {
        return Map.of(
            "localAuthority", "Example Local Authority",
            "reference", CASE_REFERENCE.toString(),
            "caseUrl", String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, CASE_REFERENCE)
        );
    }
}
