package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
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
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerSubmittedTest extends AbstractControllerTest {
    private static final String HMCTS_ADMIN_EMAIL = "admin@family-court.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String SURVEY_LINK = "https://www.smartsurvey.co"
        + ".uk/s/preview/FamilyPublicLaw/44945E4F1F8CBEE3E10D79A4CED903";

    private final Long caseId = nextLong();

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    CaseSubmissionControllerSubmittedTest() {
        super("case-submission");
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValues() {
        Map<String, Object> expectedHmctsParameters = getExpectedHmctsParameters(true);
        Map<String, Object> completeCafcassParameters = getExpectedCafcassParameters(true);

        postSubmittedEvent(buildCallbackRequest(populatedCaseDetails(Map.of("id", caseId)), OPEN));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedHmctsParameters,
                caseId.toString());

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                completeCafcassParameters,
                caseId.toString());
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = getExpectedHmctsParameters(false);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedIncompleteHmctsParameters,
                caseId.toString());

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                getExpectedCafcassParameters(false),
                caseId.toString());
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));

    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = getExpectedHmctsParameters(false);

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                CTSC_EMAIL,
                expectedIncompleteHmctsParameters,
                caseId.toString()
            ));

        checkThat(() ->
            verify(notificationClient, never()).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedIncompleteHmctsParameters,
                caseId.toString()
            ));
    }

    @Test
    void shouldMakePaymentOfAnOpenCaseWhenAmountToPayWasDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkUntil(() ->
            verify(paymentService).makePaymentForCaseOrders(caseId,
                mapper.convertValue(caseDetails.getData(), CaseData.class)));
    }

    @Test
    void shouldNotMakePaymentOfAnOpenCaseWhenAmountToPayWasNotDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(paymentService, never()).makePaymentForCaseOrders(any(), any()));
    }

    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        doThrow(new PaymentsApiException("", new Throwable())).when(paymentService)
            .makePaymentForCaseOrders(any(), any());

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
                "local-authority@local-authority.com",
                Map.of("applicationType", "C110a"),
                caseId.toString());

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                caseId.toString());
        });
    }

    @Test
    void shouldNotSendFailedPaymentNotificationWhenDisplayAmountToPayNotSet() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        checkThat(() -> {
            verify(notificationClient, never()).sendEmail(
                eq(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA),
                anyString(),
                anyMap(),
                eq(caseId.toString()));

            verify(notificationClient, never()).sendEmail(
                eq(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC),
                anyString(),
                anyMap(),
                eq(caseId.toString()));
        });

    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
                "local-authority@local-authority.com",
                Map.of("applicationType", "C110a"),
                caseId.toString());

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                caseId.toString());
        });
    }

    @Test
    void shouldPopulateResponseWithMarkdown() {
        String caseName = "Names are hard";
        CallbackRequest request = buildCallbackRequest(populatedCaseDetails(
            Map.of("caseName", caseName)
        ), OPEN);

        SubmittedCallbackResponse response = postSubmittedEvent(request);
        String expectedHeader = "# Application sent\n\n## " + caseName;
        String expectedBody = "## What happens next\n\n"
            + "We’ll check your application – we might need to ask you more questions, or send it back to you to amend."
            + "\n\nIf we have no questions, we’ll send your application to the local court gatekeeper.\n\n"
            + "You can contact us at contactFPL@justice.gov.uk.\n\n"
            + "## Help us improve this service\n\n"
            + "Tell us how this service was today on our <a href=\"" + SURVEY_LINK + "\" target=\"_blank\">feedback "
            + "form</a>.";

        assertThat(response).extracting("confirmationHeader", "confirmationBody")
            .containsExactly(expectedHeader, expectedBody);
    }

    @Nested
    class CaseResubmission {

        final State state = RETURNED;

        @BeforeEach
        void init() {
            when(documentDownloadService.downloadDocument(any())).thenReturn(DOCUMENT_CONTENT);
        }

        @Test
        void shouldNotifyAdminAndCafcassWhenCaseIsResubmitted() {
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
            caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

            CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, state);
            postSubmittedEvent(callbackRequest);

            checkUntil(() -> resubmissionNotificationsSent(HMCTS_ADMIN_EMAIL));
            checkThat(this::paymentNotTakenAndNoMoreEmailsSent);
        }

        @Test
        void shouldNotifyCtscAndCafcassWhenCaseIsResubmitted() {
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
            caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

            CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, state);
            postSubmittedEvent(callbackRequest);

            checkUntil(() -> resubmissionNotificationsSent(CTSC_EMAIL));
            checkThat(this::paymentNotTakenAndNoMoreEmailsSent);
        }

        private void resubmissionNotificationsSent(String adminEmail) throws Exception {
            verify(notificationClient).sendEmail(
                eq(AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE),
                eq(adminEmail),
                anyMap(),
                eq(caseId.toString()));

            verify(notificationClient).sendEmail(
                eq(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(caseId.toString()));
        }

        private void paymentNotTakenAndNoMoreEmailsSent() {
            verifyNoMoreInteractions(notificationClient);
            verifyNoMoreInteractions(paymentService);
        }
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C110a",
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/" + caseId);
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(caseId)
            .data(new HashMap<>(Map.of(
                "submittedForm", TestDataHelper.testDocumentReference(),
                RETURN_APPLICATION, ReturnApplication.builder()
                    .note("Some note")
                    .reason(List.of(INCOMPLETE))
                    .document(TestDataHelper.testDocumentReference())
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

        submitCaseHmctsTemplate.setCourt(DEFAULT_LA_COURT);
        return submitCaseHmctsTemplate.toMap(mapper);
    }

    private Map<String, Object> getExpectedCafcassParameters(boolean completed) {
        SubmitCaseCafcassTemplate submitCaseCafcassTemplate;

        if (completed) {
            submitCaseCafcassTemplate = getCompleteParameters(new SubmitCaseCafcassTemplate());
        } else {
            submitCaseCafcassTemplate = getIncompleteParameters(new SubmitCaseCafcassTemplate());
        }

        submitCaseCafcassTemplate.setCafcass(DEFAULT_CAFCASS_COURT);
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
        template.setReference(caseId.toString());
        template.setCaseUrl(String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, caseId));
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
