package uk.gov.hmcts.reform.fpl.controllers;

import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.CONTACT_WITH_NAMED_PERSON;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
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
        Map<String, Object> expectedHmctsParameters = getExpectedHmctsParameters(true);
        Map<String, Object> completeCafcassParameters = getExpectedCafcassParameters(true);

        postSubmittedEvent("core-case-data-store-api/callback-request.json");

        verify(notificationClient).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            expectedHmctsParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            CAFCASS_EMAIL,
            completeCafcassParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedHmctsParameters,
            CASE_REFERENCE.toString());
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() throws Exception {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = getExpectedHmctsParameters(false);

        verify(notificationClient).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            expectedIncompleteHmctsParameters,
            CASE_REFERENCE.toString());

        verify(notificationClient).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            CAFCASS_EMAIL,
            getExpectedCafcassParameters(false),
            CASE_REFERENCE.toString());

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_EMAIL,
            expectedIncompleteHmctsParameters,
            CASE_REFERENCE.toString());
    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() throws Exception {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = getExpectedHmctsParameters(false);

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            HMCTS_ADMIN_EMAIL,
            expectedIncompleteHmctsParameters,
            CASE_REFERENCE.toString()
        );

        verify(notificationClient).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_EMAIL,
            expectedIncompleteHmctsParameters,
            CASE_REFERENCE.toString()
        );
    }

    @Test
    void shouldMakePaymentOfAnOpenCaseWhenFeatureToggleIsTrueAndAmountToPayWasDisplayed() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        verify(paymentService).makePaymentForCaseOrders(CASE_REFERENCE,
            mapper.convertValue(caseDetails.getData(), CaseData.class));
    }

    @Test
    void shouldNotMakePaymentOfAnOpenCaseWhenFeatureToggleIsFalse() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(false);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        verify(paymentService, never()).makePaymentForCaseOrders(any(), any());
    }

    @Test
    void shouldNotMakePaymentOfAnOpenCaseWhenAmountToPayWasNotDisplayed() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        verify(paymentService, never()).makePaymentForCaseOrders(any(), any());
    }

    @Test
    void shouldNotMakePaymentOnAReturnedCaseWhenFeatureToggleIsTrueAndAmountToPayWasDisplayed() {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, RETURNED);

        postSubmittedEvent(callbackRequest);

        verify(paymentService, never()).makePaymentForCaseOrders(any(), any());
    }
    
    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() throws NotificationClientException {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        doThrow(new PaymentsApiException("", new Throwable())).when(paymentService)
            .makePaymentForCaseOrders(any(), any());

        postSubmittedEvent(callbackRequest);

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

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never()).sendEmail(
            eq(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA),
            anyString(),
            anyMap(),
            anyString());

        verify(notificationClient, never()).sendEmail(
            eq(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC),
            anyString(),
            anyMap(),
            anyString());
    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() throws NotificationClientException {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

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
    void shouldNotifyHmctsAdminWhenTheLocalAuthorityHasSubmittedAReturnedCase() throws Exception {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, RETURNED);
        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(HMCTS_ADMIN_EMAIL),
            anyMap(),
            eq("12345"));

        verify(notificationClient, never()).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(CTSC_EMAIL),
            anyMap(),
            eq("12345"));
    }

    @Test
    void shouldNotifyCtscAdminWhenTheLocalAuthorityHasSubmittedAReturnedCase() throws Exception {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, RETURNED);
        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never()).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(HMCTS_ADMIN_EMAIL),
            anyMap(),
            eq("12345"));

        verify(notificationClient).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(CTSC_EMAIL),
            anyMap(),
            eq("12345"));
    }

    @Test
    void shouldNotNotifyAdminOfAReturnedCaseWhenTheCaseIsSubmittedFromOpenState() throws Exception {
        given(ldClient.boolVariation(eq("payments"), any(), anyBoolean())).willReturn(true);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put("displayAmountToPay", NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never()).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(HMCTS_ADMIN_EMAIL),
            anyMap(),
            eq("12345"));

        verify(notificationClient, never()).sendEmail(
            eq(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN),
            eq(CTSC_EMAIL),
            anyMap(),
            eq("12345"));
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C110a",
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345");
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(new HashMap<>(Map.of(
                RETURN_APPLICATION, ReturnApplication.builder()
                    .note("Some note")
                    .reason(List.of(INCOMPLETE))
                    .build(),
                "orders", Orders.builder()
                        .emergencyProtectionOrderDirections(List.of(CONTACT_WITH_NAMED_PERSON))
                        .orderType(List.of(EMERGENCY_PROTECTION_ORDER))
                    .build(),
                "caseLocalAuthority", "example",
                "sendToCtsc", enableCtsc.getValue()
            ))).build();
    }

    private Map<String, Object> getExpectedHmctsParameters(boolean completed) {
        SubmitCaseHmctsTemplate submitCaseHmctsTemplate;

        if (completed) {
            submitCaseHmctsTemplate = getCompleteParameters(new SubmitCaseHmctsTemplate());
        } else {
            submitCaseHmctsTemplate = getIncompleteParameters(new SubmitCaseHmctsTemplate());
        }

        submitCaseHmctsTemplate.setCourt(FAMILY_COURT);
        return submitCaseHmctsTemplate.toMap(mapper);
    }

    private Map<String, Object> getExpectedCafcassParameters(boolean completed) {
        SubmitCaseCafcassTemplate submitCaseCafcassTemplate;

        if (completed) {
            submitCaseCafcassTemplate = getCompleteParameters(new SubmitCaseCafcassTemplate());
        } else {
            submitCaseCafcassTemplate = getIncompleteParameters(new SubmitCaseCafcassTemplate());
        }

        submitCaseCafcassTemplate.setCafcass(CAFCASS_COURT);
        return submitCaseCafcassTemplate.toMap(mapper);
    }

    private <T extends SharedNotifyTemplate> T getCompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(YES.getValue());
        template.setTimeFrameValue("same day");
        template.setUrgentHearing(YES.getValue());
        template.setNonUrgentHearing(NO.getValue());
        template.setFirstRespondentName("Smith");

        return template;
    }

    private <T extends SharedNotifyTemplate> T getIncompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(NO.getValue());
        template.setTimeFrameValue("");
        template.setUrgentHearing(NO.getValue());
        template.setNonUrgentHearing(NO.getValue());
        template.setFirstRespondentName("");

        return template;
    }

    private <T extends SharedNotifyTemplate> void setSharedTemplateParameters(T template) {
        template.setLocalAuthority("Example Local Authority");
        template.setReference(CASE_REFERENCE.toString());
        template.setCaseUrl(String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, CASE_REFERENCE));
        template.setDataPresent(YES.getValue());
        template.setFullStop(NO.getValue());
        template.setOrdersAndDirections(List.of("Emergency protection order", "Contact with any named person"));
    }

    private CallbackRequest buildCallbackRequest(CaseDetails caseDetails, State stateBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(CaseDetails.builder()
                .state(stateBefore.getValue())
                .build())
            .build();
    }
}
