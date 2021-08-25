
package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UploadAdditionalApplicationsSubmittedControllerTest extends AbstractCallbackTest {

    private static final String RESPONDENT_FIRSTNAME = "John";
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final Long CASE_ID = 12345L;
    private static final FeesData FEES_DATA = FeesData.builder().totalAmount(BigDecimal.TEN).build();
    private static final UUID LETTER_1_ID = randomUUID();
    private static final Document ORDER_DOCUMENT = testDocument();
    private static final Document COVERSHEET_OTHER_REPRESENTATIVE = testDocument();
    private static final byte[] ORDER_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_OTHER_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final DocumentReference ORDER = testDocumentReference();

    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private FeeService feeService;
    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private DocmosisCoverDocumentsService documentService;
    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequest;
    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDetails;
    @MockBean
    private SendLetterApi sendLetterApi;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    private static final Element<Representative> REPRESENTATIVE_WITH_DIGITAL_PREFERENCE = element(
        Representative.builder()
            .fullName("Digital Representative ")
            .servingPreferences(DIGITAL_SERVICE)
            .email("digital-rep@test.com")
            .build());
    private static final Element<Representative> REPRESENTATIVE_WITH_EMAIL_PREFERENCE = element(
        Representative.builder()
            .fullName("Email Representative")
            .servingPreferences(EMAIL)
            .email("email-rep@test.com")
            .build());
    private static final Element<Respondent> RESPONDENT_WITH_DIGITAL_REP = element(Respondent.builder()
        .party(RespondentParty.builder().firstName("George").lastName("Jones").address(testAddress()).build())
        .representedBy(wrapElements(REPRESENTATIVE_WITH_DIGITAL_PREFERENCE.getId()))
        .build());
    private static final Element<Respondent> RESPONDENT_WITH_EMAIL_REP = element(Respondent.builder()
        .representedBy(wrapElements(REPRESENTATIVE_WITH_EMAIL_PREFERENCE.getId()))
        .party(RespondentParty.builder().firstName("Alex").lastName("Jones").address(testAddress()).build())
        .build());
    private static final Element<Respondent> UNREPRESENTED_RESPONDENT = element(Respondent.builder()
        .party(RespondentParty.builder().firstName("Emma").lastName("Jones").build())
        .build());
    private static final Element<Representative> REPRESENTATIVE_WITH_POST_PREFERENCE = element(
        Representative.builder()
            .fullName("Respondent PostRep1")
            .servingPreferences(POST)
            .address(testAddress())
            .build());
    private static final Element<Representative> OTHER_REP_BY_POST = element(Representative.builder()
        .fullName("Other PostRep2")
        .servingPreferences(POST)
        .address(testAddress())
        .build());
    private static final Element<Respondent> RESPONDENT_WITH_POST_REP = element(Respondent.builder()
        .party(RespondentParty.builder().firstName("Tim").lastName("Jones").address(testAddress()).build())
        .representedBy(wrapElements(REPRESENTATIVE_WITH_POST_PREFERENCE.getId()))
        .build());

    private final Other other = Other.builder().address(testAddress()).name("Emily Jones").build();

    UploadAdditionalApplicationsSubmittedControllerTest() {
        super("upload-additional-applications");
    }

    @BeforeEach
    void init() {
        other.addRepresentative(OTHER_REP_BY_POST.getId());
        givenFplService();
        given(documentDownloadService.downloadDocument(ORDER.getBinaryUrl())).willReturn(ORDER_BINARY);
        given(uploadDocumentService.uploadPDF(ORDER_BINARY, ORDER.getFilename()))
            .willReturn(ORDER_DOCUMENT);
        given(documentService.createCoverDocuments(any(), any(), eq(OTHER_REP_BY_POST.getValue())))
            .willReturn(DocmosisDocument.builder().bytes(COVERSHEET_OTHER_REPRESENTATIVE_BINARY).build());
        given(uploadDocumentService.uploadPDF(COVERSHEET_OTHER_REPRESENTATIVE_BINARY, "Coversheet.pdf"))
            .willReturn(COVERSHEET_OTHER_REPRESENTATIVE);
        given(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_1_ID));
    }

    @Test
    void submittedEventShouldNotifyHmctsAdminAndRepresentativesWhenCtscToggleIsDisabled() {
        List<Element<Respondent>> respondents = List.of(RESPONDENT_WITH_DIGITAL_REP, RESPONDENT_WITH_EMAIL_REP,
            RESPONDENT_WITH_POST_REP, UNREPRESENTED_RESPONDENT);
        CaseData caseData = CaseData.builder().id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .familyManCaseNumber(String.valueOf(CASE_ID))
            .respondents1(respondents)
            .representatives(List.of(REPRESENTATIVE_WITH_DIGITAL_PREFERENCE, REPRESENTATIVE_WITH_EMAIL_PREFERENCE,
                REPRESENTATIVE_WITH_POST_PREFERENCE, OTHER_REP_BY_POST))
            .others(Others.builder().firstOther(other).build())
            .additionalApplicationType(List.of(C2_ORDER))
            .sendToCtsc("No")
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .pbaPayment(PBAPayment.builder().usePbaPayment("Yes").build())
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .document(ORDER)
                    .supplementsBundle(new ArrayList<>())
                    .others(List.of(element(other)))
                    .respondents(List.of(
                        RESPONDENT_WITH_DIGITAL_REP, RESPONDENT_WITH_EMAIL_REP, UNREPRESENTED_RESPONDENT))
                    .applicantName(LOCAL_AUTHORITY_1_NAME + ", Applicant").build())
                .build())).build();

        postSubmittedEvent(caseData);
        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC),
            eq(COURT_1.getEmail()),
            anyMap(),
            eq(notificationReference(CASE_ID))
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(notificationReference(CASE_ID))));

        checkUntil(() -> verify(notificationClient, never()).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS),
            eq("digital-rep@test.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS),
            eq("email-rep@test.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))));

        checkUntil(() -> verify(sendLetterApi).sendLetter(eq(SERVICE_AUTH_TOKEN), printRequest.capture()));
        checkUntil(() -> verify(coreCaseDataService).updateCase(eq(CASE_ID), caseDetails.capture()));

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(
            CASE_ID, ORDER, COVERSHEET_OTHER_REPRESENTATIVE_BINARY, ORDER_BINARY);

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest2));

        final CaseData caseUpdate = getCase(this.caseDetails);
        SentDocument expectedDocumentSentToRespondent = documentSent(OTHER_REP_BY_POST.getValue(),
            COVERSHEET_OTHER_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_1_ID, now());

        assertThat(caseUpdate.getDocumentsSentToParties()).hasSize(1);
        assertThat(caseUpdate.getDocumentsSentToParties().get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRespondent);
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWithUpdatedTemplate() {
        postSubmittedEvent(buildCaseDetails(YES, YES));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))
        ));
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenCtscToggleIsEnabled() {
        postSubmittedEvent(buildCaseDetails(YES, YES));
        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(notificationReference(CASE_ID))
        ));
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenAdditionalApplicationsBundleIsUploaded() {
        final Map<String, Object> caseData = ImmutableMap.of(
            "caseLocalAuthority",
            LOCAL_AUTHORITY_1_CODE,
            "additionalApplicationType",
            List.of(OTHER_ORDER),
            "additionalApplicationsBundle",
            wrapElements(
                AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                        .build())
                    .pbaPayment(PBAPayment.builder().build()).build()));

        postSubmittedEvent(createCase(caseData));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC),
            eq(COURT_1.getEmail()),
            anyMap(),
            eq(notificationReference(CASE_ID))));
    }

    @Test
    void submittedEventShouldNotifyAdminWhenWhenAdditionalApplicationsAreNotUsingPbaPayment() throws Exception {
        final Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(NO))
            .put("displayAmountToPay", YES.getValue())
            .build();

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            COURT_1.getEmail(),
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verify(notificationClient, never()).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    void submittedEventShouldNotifyCtscAdminWhenAdditionalApplicationsAreNotUsingPbaPaymentAndCtscToggleIsEnabled()
        throws Exception {
        postSubmittedEvent(buildCaseDetails(YES, NO));

        verify(notificationClient, never()).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            COURT_1.getEmail(),
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );
    }

    @Test
    void submittedEventShouldNotNotifyAdminWhenAdditionalApplicationsAreUsingPbaPayment() throws Exception {
        postSubmittedEvent(buildCaseDetails(NO, YES));

        verify(notificationClient, never()).sendEmail(
            eq(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE),
            anyString(),
            anyMap(),
            anyString()
        );
    }

    @Test
    void submittedEventShouldNotNotifyAdminWhenPbaPaymentIsNull() throws Exception {
        CaseData caseData = CaseData.builder().caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber(String.valueOf(CASE_ID))
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(RESPONDENT_FIRSTNAME)
                    .lastName(RESPONDENT_SURNAME)
                    .build())
                .build())))
            .additionalApplicationType(List.of(C2_ORDER))
            .additionalApplicationsBundle(wrapElements(
                AdditionalApplicationsBundle.builder()
                    .pbaPayment(null)
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .type(WITH_NOTICE)
                        .supplementsBundle(new ArrayList<>())
                        .usePbaPayment(NO.getValue())
                        .applicantName(LOCAL_AUTHORITY_1_NAME + ", Applicant")
                        .build())
                    .build()))
            .build();

        postSubmittedEvent(caseData);

        verify(notificationClient, never()).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            COURT_1.getEmail(),
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verify(notificationClient, never()).sendEmail(
            INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
            "FamilyPublicLaw+ctsc@gmail.com",
            expectedPbaPaymentNotTakenNotificationParams(),
            notificationReference(CASE_ID)
        );

        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldMakePaymentWhenAmountToPayWasDisplayedFor() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(buildCommonNotificationParameters())
            .putAll(buildAdditionalApplicationsBundle(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        CaseDetails caseDetails = createCase(caseData);

        when(feeService.getFeesDataForAdditionalApplications(any()))
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
            .putAll(buildAdditionalApplicationsBundleWithC2AndOtherOrder(YES))
            .put("displayAmountToPay", YES.getValue())
            .build();

        doThrow(new PaymentsApiException("", new Throwable()))
            .when(paymentService).makePaymentForAdditionalApplications(any(), any(), any());

        postSubmittedEvent(createCase(caseData));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            LOCAL_AUTHORITY_1_INBOX,
            Map.of("applicationType", "C2, C1 - Appointment of a guardian",
                "caseUrl", caseUrl(CASE_ID, "Other applications")),
            notificationReference(CASE_ID));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            "FamilyPublicLaw+ctsc@gmail.com",
            Map.of("applicationType", "C2, C1 - Appointment of a guardian",
                "caseUrl", caseUrl(CASE_ID, "Other applications"),
                "applicant", LOCAL_AUTHORITY_1_NAME),
            notificationReference(CASE_ID));
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
            "caseLocalAuthorityName", LOCAL_AUTHORITY_1_NAME,
            "familyManCaseNumber", String.valueOf(CASE_ID),
            "respondents1", List.of(element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(RESPONDENT_FIRSTNAME)
                    .lastName(RESPONDENT_SURNAME)
                    .build())
                .build())));
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
                        .type(WITH_NOTICE)
                        .supplementsBundle(new ArrayList<>())
                        .applicantName(LOCAL_AUTHORITY_1_NAME + ", Applicant").build())
                    .build()));
    }

    private Map<String, Object> buildAdditionalApplicationsBundleWithC2AndOtherOrder(YesNo usePbaPayment) {
        return ImmutableMap.of(
            "additionalApplicationType", List.of(C2_ORDER, OTHER_ORDER),
            "additionalApplicationsBundle", wrapElements(
                AdditionalApplicationsBundle.builder()
                    .pbaPayment(PBAPayment.builder().usePbaPayment(usePbaPayment.getValue()).build())
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .type(WITH_NOTICE)
                        .supplementsBundle(new ArrayList<>())
                        .applicantName(LOCAL_AUTHORITY_1_NAME + ", Applicant").build())
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                        .supplementsBundle(new ArrayList<>())
                        .applicantName(LOCAL_AUTHORITY_1_NAME + ", Applicant").build())
                    .build()));
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
        return Map.of("caseUrl", caseUrl(CASE_ID, "Other applications"));
    }
}
