
package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsSubmittedControllerTest extends AbstractControllerTest {

    private static final UserInfo USER_INFO_CAFCASS = UserInfo.builder().roles(UserRole.CAFCASS.getRoles()).build();
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final Long CASE_ID = 12345L;
    private static DocumentReference applicationDocument;
    private static DocumentReference latestC2Document;
    private static final byte[] C2_BINARY = {5, 4, 3, 2, 1};

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    UploadC2DocumentsSubmittedControllerTest() {
        super("upload-c2");
    }

    @BeforeEach
    void setup() {
        given(idamApi.retrieveUserInfo(any())).willReturn(USER_INFO_CAFCASS);

        applicationDocument = testDocumentReference();
        latestC2Document = testDocumentReference();
        when(documentDownloadService.downloadDocument(latestC2Document.getBinaryUrl()))
            .thenReturn(C2_BINARY);
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminWhenCtscToggleIsDisabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, YES));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "admin@family-court.com",
            buildExpectedNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            buildExpectedNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, YES));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "admin@family-court.com",
            buildExpectedNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com", buildExpectedNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyAdminWhenC2IsNotUsingPbaPayment() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, NO));

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenC2IsNotUsingPbaPaymentAndCtscToggleIsEnabled() throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, NO));

        verify(notificationClient, never()).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "admin@family-court.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
        );

        verify(notificationClient).sendEmail(
            C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            CASE_ID.toString()
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

        postSubmittedEvent(createCase(caseData));

        verify(paymentService).makePaymentForC2(CASE_ID, mapper.convertValue(caseData, CaseData.class));
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
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C2"),
            "12345");

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            "12345");
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
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C2"),
            "12345");

        verify(notificationClient).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedCtscNotificationParameters(),
            "12345");
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
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "familyManCaseNumber", String.valueOf(CASE_ID),
            "submittedForm", Map.of("document_url", "http://dm-store:8080/documents/be17a76e-38ed-4448-8b83-45de1aa93f55",
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
                .build())
        );
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2",
            "caseUrl", "http://fake-url/cases/case-details/12345");
    }

    private Map<String, Object> buildExpectedNotificationParams() {
        C2UploadedTemplate c2UploadedTemplate = new C2UploadedTemplate();

        c2UploadedTemplate.setCallout(String.format("%s, %s", RESPONDENT_SURNAME, CASE_ID.toString()));
        c2UploadedTemplate.setRespondentLastName("Watson");
        c2UploadedTemplate.setCaseUrl("http://fake-url/cases/case-details/" + CASE_ID);
        c2UploadedTemplate.setDocumentLink(generateAttachedDocumentLink(C2_BINARY)
            .map(JSONObject::toMap)
            .orElse(null));

        return c2UploadedTemplate.toMap(mapper);
    }

    private Map<String, Object> expectedPbaPaymentNotTakenNotificationParams() {
        return Map.of(
            "caseUrl", "http://fake-url/cases/case-details/" + CASE_ID
        );
    }
}
