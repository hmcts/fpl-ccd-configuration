
package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsSubmittedControllerTest extends AbstractCallbackTest {

    private static final UserInfo USER_INFO_CAFCASS = UserInfo.builder().roles(UserRole.CAFCASS.getRoleNames()).build();
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final Long CASE_ID = 12345L;
    private static DocumentReference applicationDocument;
    private static DocumentReference latestC2Document;
    private static final byte[] C2_BINARY = {5, 4, 3, 2, 1};
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    public static final FeesData FEES_DATA = FeesData.builder().totalAmount(BigDecimal.TEN).build();

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private IdamClient idamClient;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    UploadAdditionalApplicationsSubmittedControllerTest() {
        super("upload-additional-applications");
    }

    @BeforeEach
    void setup() {
        given(idamClient.getUserInfo((USER_AUTH_TOKEN))).willReturn(USER_INFO_CAFCASS);

        applicationDocument = testDocumentReference();
        latestC2Document = testDocumentReference();
        when(documentDownloadService.downloadDocument(latestC2Document.getBinaryUrl()))
            .thenReturn(C2_BINARY);
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminWhenCtscToggleIsDisabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, YES));

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("admin@family-court.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, YES));

        verify(notificationClient, never()).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("admin@family-court.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );

        verify(notificationClient).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );
    }

    @Test
    void submittedEventShouldNotifyWhenAdditionalApplciationsBundleDoesNotHaveC2DocumentBundle() {
        final Map<String, Object> caseData = ImmutableMap.of(
            "additionalApplicationType", List.of(OTHER_ORDER),
            "additionalApplicationsBundle", wrapElements(
                AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder().build()).build()));

        postSubmittedEvent(createCase(caseData));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void submittedEventShouldNotifyAdminWhenC2IsNotUsingPbaPayment() throws Exception {
        final Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(NO))
            .put("displayAmountToPay", YES.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            NOTIFICATION_REFERENCE
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            NOTIFICATION_REFERENCE
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
            NOTIFICATION_REFERENCE
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            NOTIFICATION_REFERENCE
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
    void shouldMakePaymentWhenAmountToPayWasDisplayedFor() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        CaseDetails caseDetails = createCase(caseData);

        when(feeService.getFeesDataForAdditionalApplications(any(), any(), any(), any()))
            .thenReturn(FEES_DATA);

        postSubmittedEvent(caseDetails);

        verify(paymentService).makePaymentForAdditionalApplications(
            CASE_ID, caseConverter.convert(caseDetails), FEES_DATA);
    }

    @Test
    void shouldNotMakePaymentWhenAmountToPayWasNotDisplayed() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
            .put("displayAmountToPay", NO.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(paymentService, never()).makePaymentForC2(any(), any());
    }

    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        doThrow(new PaymentsApiException("", new Throwable()))
            .when(paymentService).makePaymentForAdditionalApplications(any(), any(), any());

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_1_INBOX,
            Map.of("applicationType", "C2"),
            NOTIFICATION_REFERENCE);

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            NOTIFICATION_REFERENCE);
    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
            .put("displayAmountToPay", NO.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_1_INBOX,
            Map.of("applicationType", "C2"),
            NOTIFICATION_REFERENCE);

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            NOTIFICATION_REFERENCE);
    }

    @Test
    void shouldNotSendFailedPaymentNotificationWhenDisplayAmountToPayNotSet() throws NotificationClientException {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
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
            .putAll(buildAdditionalApplicationsBundle(usePbaPayment))
            .put("sendToCtsc", enableCtsc.getValue())
            .build());
    }

    private Map<String, Object> buildCommonNotificationParameters() {
        return Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
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

    private Map<String, Object> buildAdditionalApplicationsBundle(YesNo usePbaPayment) {
        return ImmutableMap.of(
            "additionalApplicationType", List.of(C2_ORDER),
            "additionalApplicationsBundle", wrapElements(
                AdditionalApplicationsBundle.builder()
                    .pbaPayment(PBAPayment.builder().usePbaPayment(usePbaPayment.getValue()).build())
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .document(latestC2Document)
                        .usePbaPayment(usePbaPayment.getValue()).build())
                    .build()));
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2",
            "caseUrl", "http://fake-url/cases/case-details/12345#C2");
    }

    private Map<String, Object> expectedPbaPaymentNotTakenNotificationParams() {
        return Map.of("caseUrl", "http://fake-url/cases/case-details/12345#C2");
    }
}
