
package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsSubmittedControllerTest extends AbstractCallbackTest {

    private static final UserInfo USER_INFO_CAFCASS = UserInfo.builder().roles(UserRole.CAFCASS.getRoleNames()).build();
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final Long CASE_ID = 12345L;
    private static DocumentReference applicationDocument;
    private static DocumentReference latestC2Document;
    private static final byte[] C2_BINARY = testDocumentBinaries();

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private WorkAllocationTaskService workAllocationTaskService;

    @MockBean
    private FeatureToggleService featureToggleService;

    UploadC2DocumentsSubmittedControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void setup() {
        givenCurrentUser(USER_INFO_CAFCASS);

        applicationDocument = testDocumentReference();
        latestC2Document = testDocumentReference();
        when(documentDownloadService.downloadDocument(latestC2Document.getBinaryUrl()))
            .thenReturn(C2_BINARY);

        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminWhenCtscToggleIsDisabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, YES));

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq(COURT_1.getEmail()),
            anyMap(),
            eq(notificationReference(CASE_ID))
        );

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, YES));

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("admin@family-court.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))
        );

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))
        );
    }

    @Test
    void submittedEventShouldNotifyAdminWhenC2IsNotUsingPbaPayment() throws Exception {
        final Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(NO))
            .put("displayAmountToPay", YES.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            COURT_1.getEmail(),
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenC2IsNotUsingPbaPaymentAndCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, NO));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );
    }

    @Test
    void submittedEventShouldNotNotifyAdminWhenUC2IsUsingPbaPayment() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, YES));

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE),
            anyString(),
            anyMap(),
            anyString()
        );
    }

    @Test
    void shouldMakePaymentWhenAmountToPayWasDisplayed() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        CaseDetails caseDetails = createCase(caseData);

        postSubmittedEvent(caseDetails);

        verify(paymentService).makePaymentForC2(CASE_ID, caseConverter.convert(caseDetails));
    }

    @Test
    void shouldNotMakePaymentWhenAmountToPayWasNotDisplayed() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .put("displayAmountToPay", NO.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(paymentService, never()).makePaymentForC2(any(), any());
    }

    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        doThrow(new PaymentsApiException("", new Throwable())).when(paymentService).makePaymentForC2(any(), any());

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            LOCAL_AUTHORITY_1_INBOX,
            expectedApplicantNotificationParameters(),
            notificationReference(CASE_ID));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            notificationReference(CASE_ID));
    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .put("displayAmountToPay", NO.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            LOCAL_AUTHORITY_1_INBOX,
            expectedApplicantNotificationParameters(),
            notificationReference(CASE_ID));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            notificationReference(CASE_ID));
    }

    @Test
    void shouldNotSendFailedPaymentNotificationWhenDisplayAmountToPayNotSet() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(YES))
            .build();

        postSubmittedEvent(createCase(caseData));

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

    private CaseDetails buildCaseDetails(YesNo enableCtsc, YesNo usePbaPayment) {
        return createCase(ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildC2DocumentBundle(usePbaPayment))
            .put("sendToCtsc", enableCtsc.getValue())
            .build());
    }

    private Map<String, Object> buildCommonNotificationParameters() {
        return Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "caseLocalAuthorityName", LOCAL_AUTHORITY_1_NAME,
            "localAuthorities", wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()),
            "familyManCaseNumber", String.valueOf(CASE_ID),
            "submittedForm",
            Map.of("document_url", "http://dm-store:8080/documents/be17a76e-38ed-4448-8b83-45de1aa93f55",
                "document_filename", "form.pdf",
                "document_binary_url", applicationDocument.getBinaryUrl()),
            "respondents1", List.of(
                Map.of(
                    "value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName(RESPONDENT_SURNAME)
                            .build())
                        .build()))
        );
    }

    private CaseDetails createCase(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }

    private Map<String, Object> buildC2DocumentBundle(YesNo usePbaPayment) {
        return ImmutableMap.of(
            "c2DocumentBundle", wrapElements(C2DocumentBundle.builder()
                .document(latestC2Document)
                .usePbaPayment(usePbaPayment.getValue())
                .applicantName(LOCAL_AUTHORITY_1_NAME)
                .build())
        );
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2",
            "caseUrl", caseUrl(CASE_ID, "Other applications"),
            "applicant", LOCAL_AUTHORITY_1_NAME);
    }

    private Map<String, Object> expectedApplicantNotificationParameters() {
        return Map.of("applicationType", "C2",
            "caseUrl", caseUrl(CASE_ID, "Other applications"));
    }

    private Map<String, Object> expectedPbaPaymentNotTakenNotificationParams() {
        return Map.of("caseUrl", caseUrl(CASE_ID, "C2"));
    }
}
