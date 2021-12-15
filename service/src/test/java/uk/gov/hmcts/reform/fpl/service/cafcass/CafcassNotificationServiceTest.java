package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;

@ExtendWith(MockitoExtension.class)
class CafcassNotificationServiceTest {
    private static final String SENDER_EMAIL = "senderEmail";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String DOCUMENT_BINARY_URL = "originalDocumentBinaryUrl";
    private static final byte[] DOCUMENT_CONTENT = "OriginalDocumentContent".getBytes();
    private static final String DOCUMENT_FILENAME = "fileToSend.pdf";
    private static final String FAMILY_MAN = "FM1234";
    private static final String TITLE = "dummy";
    private  static final long CASE_ID = 12345L;

    @Mock
    private EmailService emailService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private CafcassEmailConfiguration configuration;

    @InjectMocks
    private CafcassNotificationService underTest;

    @Captor
    private ArgumentCaptor<EmailData> emailData;

    @Test
    void shouldNotifyOrderRequest() {
        when(configuration.getRecipientForOrder()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
            DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(FAMILY_MAN)
            .build();

        underTest.sendEmail(caseData,
            of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .filename(DOCUMENT_FILENAME)
                .build()),
            ORDER,
            OrderCafcassData.builder()
                .documentName(TITLE)
                .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- new order");
        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
            String.join(" ",
                "A new order for this case was uploaded to the Public Law Portal entitled",
                TITLE)
        );
    }

    @Test
    void shouldNotifyUrgentNewApplicationRequest() {
        when(configuration.getRecipientForNewApplication()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL))
            .thenReturn(DOCUMENT_CONTENT);

        String ordersAndDirections = "• Variation or discharge of care or supervision order\n• Care order";
        NewApplicationCafcassData newApplicationCafcassData = NewApplicationCafcassData.builder()
            .firstRespondentName("James Wright")
            .eldestChildLastName("Oliver Wright")
            .timeFrameValue(SAME_DAY)
            .timeFramePresent(true)
            .localAuthourity("Swansea City Council")
            .ordersAndDirections(ordersAndDirections)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.sendEmail(caseData,
            of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .filename(DOCUMENT_FILENAME)
                .build()),
            NEW_APPLICATION,
            newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
            "Urgent application – same day hearing, Oliver Wright");

        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
             "Swansea City Council has made a new application for:\n\n"
                 + "• Variation or discharge of care or supervision order\n"
                 + "• Care order\n\n"
                 + "Hearing date requested: same day\n\n"
                 + "Respondent's surname: James Wright\n\n"
                 + "CCD case number: 12345"
        );
    }


    @Test
    void shouldNotifyNewApplicationRequestWhenNoTimeFramePresent() {
        when(configuration.getRecipientForNewApplication()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL))
                .thenReturn(DOCUMENT_CONTENT);

        String ordersAndDirections = "• Variation or discharge of care or supervision order\n• Care order";
        NewApplicationCafcassData newApplicationCafcassData = NewApplicationCafcassData.builder()
                .firstRespondentName("James Wright")
                .eldestChildLastName("Oliver Wright")
                .timeFrameValue("")
                .timeFramePresent(false)
                .localAuthourity("Swansea City Council")
                .ordersAndDirections(ordersAndDirections)
                .build();

        CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .build();

        underTest.sendEmail(caseData,
                of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                        .filename(DOCUMENT_FILENAME)
                        .build()),
                NEW_APPLICATION,
                newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
                "Application received, Oliver Wright");

        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                "Swansea City Council has made a new application for:\n\n"
                        + "• Variation or discharge of care or supervision order\n"
                        + "• Care order\n\n\n\n"
                        + "Respondent's surname: James Wright\n\n"
                        + "CCD case number: 12345"
        );
    }

    @Test
    void shouldNotifyNonUrgentNewApplicationRequest() {
        when(configuration.getRecipientForNewApplication()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL))
            .thenReturn(DOCUMENT_CONTENT);

        String ordersAndDirections = "• Variation or discharge of care or supervision order\n• Care order";
        NewApplicationCafcassData newApplicationCafcassData = NewApplicationCafcassData.builder()
            .firstRespondentName("James Wright")
            .eldestChildLastName("Oliver Wright")
            .timeFrameValue("within 7 days")
            .timeFramePresent(true)
            .localAuthourity("Swansea City Council")
            .ordersAndDirections(ordersAndDirections)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.sendEmail(caseData,
            of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .filename(DOCUMENT_FILENAME)
                .build()),
            NEW_APPLICATION,
            newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
            "Application received – hearing within 7 days, Oliver Wright");
        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
             "Swansea City Council has made a new application for:\n\n"
                 + "• Variation or discharge of care or supervision order\n"
                 + "• Care order\n\n"
                 + "Hearing date requested: within 7 days\n\n"
                 + "Respondent's surname: James Wright\n\n"
                 + "CCD case number: 12345"
        );
    }

    @Test
    void shouldNotifyCourtBundle() {
        when(configuration.getRecipientForCourtBundle()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                        .filename(DOCUMENT_FILENAME)
                        .build()),
                COURT_BUNDLE,
                CourtBundleData.builder()
                        .hearingDetails(TITLE)
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- new court bundle");
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "A new court bundle for this case was uploaded to the Public Law Portal entitled",
                        TITLE)
        );
    }

    @Test
    void shouldNotifyNewDocument() {
        when(configuration.getRecipientForNewDocument()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                        .filename(DOCUMENT_FILENAME)
                        .build()),
                NEW_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Application statement")
                        .emailSubjectInfo("Further documents for main application")
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- Further documents for main application");
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "Types of documents attached:\n\n"
                                + "• Application statement")
        );
    }

    @Test
    void shouldNotifyAdditionalDocument() {
        when(configuration.getRecipientForAdditionlDocument()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                        .filename(DOCUMENT_FILENAME)
                        .build()),
                ADDITIONAL_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Additional statement")
                        .emailSubjectInfo("additional documents")
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- additional documents");
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "Types of documents attached:\n\n"
                                + "• Additional statement")
        );
    }
}
