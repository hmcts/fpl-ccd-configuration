package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.LargeFilesNotificationData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.UrgentHearingOrderAndNopData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentMetadataDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.time.LocalDate;
import java.time.Month;

import static java.time.format.FormatStyle.LONG;
import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassEmailContentProvider.URGENT_HEARING_ORDER_AND_NOP;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CHANGE_OF_ADDRESS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CafcassNotificationServiceTest {
    private static final String SENDER_EMAIL = "senderEmail";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String DOCUMENT_BINARY_URL = "originalDocumentBinaryUrl";
    private static final String DOCUMENT_URL = "originalDocumentUrl";
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
    private DocumentMetadataDownloadService documentMetadataDownloadService;
    @Mock
    private CafcassEmailConfiguration configuration;
    @Mock
    private CafcassLookupConfiguration lookupConfiguration;
    @Mock
    private CaseUrlService caseUrlService;

    private CafcassNotificationService underTest;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    @Captor
    private ArgumentCaptor<LargeFilesNotificationData> largeFilesNotificationDataArgumentCaptor;

    @BeforeEach
    void setUp() {
        underTest = new CafcassNotificationService(
                emailService,
                documentDownloadService,
                configuration,
                lookupConfiguration,
                caseUrlService,
                documentMetadataDownloadService,
                25L
        );
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentMetadataDownloadService.getDocumentMetadata(anyString()))
            .thenReturn(DocumentReference.builder().size(10L).build());
    }

    @Test
    void shouldNotifyOrderRequest() {
        when(configuration.getRecipientForOrder()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
            DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(FAMILY_MAN)
            .build();

        underTest.sendEmail(caseData,
            of(getDocumentReference()),
            ORDER,
            OrderCafcassData.builder()
                .documentName(TITLE)
                .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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

    private DocumentReference getDocumentReference() {
        return DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .url(DOCUMENT_URL)
                .filename(DOCUMENT_FILENAME)
                .build();
    }

    private DocumentReference getDocumentReference(long size) {
        return DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
            .url(DOCUMENT_URL)
            .filename(DOCUMENT_FILENAME)
            .size(size)
            .build();
    }

    @Test
    void shouldNotifyUrgentNewApplicationRequest() {
        when(configuration.getRecipientForNewApplication()).thenReturn(RECIPIENT_EMAIL);
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
            of(getDocumentReference()),
            NEW_APPLICATION,
            newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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
                of(getDocumentReference()),
                NEW_APPLICATION,
                newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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
            of(getDocumentReference()),
            NEW_APPLICATION,
            newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(getDocumentReference()),
                COURT_BUNDLE,
                CourtBundleData.builder()
                        .hearingDetails(TITLE)
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(getDocumentReference()),
                NEW_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Application statement")
                        .emailSubjectInfo("Further documents for main application")
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
                of(getDocumentReference()),
                ADDITIONAL_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Additional statement")
                        .emailSubjectInfo("additional documents")
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
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

    @Test
    void shouldNotifyNoficeOfHearing() {
        when(configuration.getRecipientForNoticeOfHearing()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);
        LocalDate hearingDate = LocalDate.of(2050, Month.APRIL,20);
        String hearingVenue = "London";
        String hearingHearingTime = "1 hour before the hearing";
        String hearingTime = "18 June, 3:40pm - 19 June, 2:30pm";

        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        underTest.sendEmail(caseData,
            of(getDocumentReference()),
            NOTICE_OF_HEARING,
            NoticeOfHearingCafcassData.builder()
                    .hearingType(CASE_MANAGEMENT.getLabel().toLowerCase())
                    .firstRespondentName("James Wright")
                    .eldestChildLastName("Oliver Wright")
                    .hearingDate(formatLocalDateToString(hearingDate, LONG))
                    .hearingVenue(hearingVenue)
                    .preHearingTime(hearingHearingTime)
                    .hearingTime(hearingTime)
                    .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- "
                + "New case management hearing Oliver Wright - notice of hearing");

        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );

        assertThat(data.getMessage()).isEqualToNormalizingWhitespace(
                String.join(" ",
                        "There’s a new  case management hearing for:\n"
                                + "James Wright FM1234"
                                + "\n\n"
                                + "Hearing details"
                                + "\n"
                                + "Date: 20 April 2050"
                                + "\n"
                                + "Venue: London"
                                + "\n"
                                + "Pre-hearing time: 1 hour before the hearing"
                                + "\n"
                                + "Hearing time: 18 June, 3:40pm - 19 June, 2:30pm")
        );
    }


    @Test
    void shouldNotifyLargeDocumentsWhenIsLargerThanConfiguredLimit() {
        long caseId = 200L;
        String caseLink = "http://localhost:8080/cases/case-details/200";

        when(configuration.getRecipientForLargeAttachements()).thenReturn(RECIPIENT_EMAIL);
        when(caseUrlService.getCaseUrl(caseId)).thenReturn(caseLink);
        when(documentMetadataDownloadService.getDocumentMetadata(anyString()))
                .thenReturn(DocumentReference.builder().size(Long.MAX_VALUE).build());


        CaseData caseData = CaseData.builder()
                .familyManCaseNumber(FAMILY_MAN)
                .id(caseId)
                .build();

        underTest.sendEmail(caseData,
                of(getDocumentReference()),
                ADDITIONAL_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Additional statement")
                        .emailSubjectInfo("additional documents")
                        .build()
        );

        verify(documentDownloadService, never()).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());

        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- new large document added");
        assertThat(data.getMessage()).isEqualTo(
                String.join("", "Large document(s) for this case was uploaded to the ",
                    "Public Law Portal entitled fileToSend.pdf. As this could ",
                    "not be sent by email you will need to download it ",
                    "from the Portal using this link.",
                    System.lineSeparator(),
                    "http://localhost:8080/cases/case-details/200")
        );
    }

    @Test
    void shouldNotifyChangeOfAddressOfChildren() {
        when(configuration.getRecipientForChangeOfAddress()).thenReturn(RECIPIENT_EMAIL);

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(FAMILY_MAN)
            .caseName("GU1234")
            .build();

        underTest.sendEmail(caseData,
            CHANGE_OF_ADDRESS,
            ChangeOfAddressData.builder().children(true).build()
        );

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- change of address - child solicitor");
        assertThat(data.getMessage()).isEqualTo(
            String.join(" ","A change of address has been added to this case",
                "which was uploaded to the Public Law Portal entitled", "[GU1234]."));
    }

    @Test
    void shouldNotifyChangeOfAddressOfRespondents() {
        when(configuration.getRecipientForChangeOfAddress()).thenReturn(RECIPIENT_EMAIL);

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(FAMILY_MAN)
            .caseName("GU1234")
            .build();

        underTest.sendEmail(caseData,
            CHANGE_OF_ADDRESS,
            ChangeOfAddressData.builder().respondents(true).build()
        );

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- change of address - respondent solicitor");
        assertThat(data.getMessage()).isEqualTo(
            String.join(" ","A change of address has been added to this case",
                "which was uploaded to the Public Law Portal entitled", "[GU1234]."));
    }

    @Test
    void shouldNotifyUrgentHearingOrder() {
        when(lookupConfiguration.getCafcass("SA"))
            .thenReturn(new CafcassLookupConfiguration.Cafcass("SA", RECIPIENT_EMAIL));
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL))
            .thenReturn(DOCUMENT_CONTENT);

        UrgentHearingOrderAndNopData urgentHearingOrderAndNopData = UrgentHearingOrderAndNopData.builder()
            .leadRespondentsName("Tim Cook")
            .callout("CALLOUT")
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority("SA")
            .build();

        underTest.sendEmail(caseData,
            of(getDocumentReference()),
            URGENT_HEARING_ORDER_AND_NOP,
            urgentHearingOrderAndNopData
        );

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.isPriority()).isTrue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
            "Urgent hearing order and notice of proceedings issued, Tim Cook");
        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf", DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
            String.join("\n\n",
                "An urgent hearing order and notice of proceedings have been issued for:\nCALLOUT",
                "Next steps",
                "You should now check the order to see your directions and compliance dates.",
                "HM Courts & Tribunals Service",
                "Do not reply to this email. If you need to contact us, call 0330 808 4424 or "
                    + "email contactfpl@justice.gov.uk")
        );
    }

    @Test
    void shouldNotifyUrgentHearingOrderWithLargeFile() {
        when(documentMetadataDownloadService.getDocumentMetadata(anyString()))
            .thenReturn(DocumentReference.builder().size(30000000L).build());
        when(lookupConfiguration.getCafcass("SA"))
            .thenReturn(new CafcassLookupConfiguration.Cafcass("SA", RECIPIENT_EMAIL));
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(caseUrlService.getCaseUrl(anyLong())).thenReturn("http://fakeurl/");

        UrgentHearingOrderAndNopData urgentHearingOrderAndNopData = UrgentHearingOrderAndNopData.builder()
            .leadRespondentsName("Tim Cook")
            .callout("CALLOUT")
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority("SA")
            .build();

        underTest.sendEmail(caseData,
            of(getDocumentReference(30L)),
            URGENT_HEARING_ORDER_AND_NOP,
            urgentHearingOrderAndNopData
        );

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.isPriority()).isTrue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
            "Urgent hearing order and notice of proceedings issued, Tim Cook");
        assertThat(data.getAttachments()).isNull();
        assertThat(data.getMessage()).isEqualTo(
            String.join("\n\n",
                String.format("An urgent hearing order and notice of proceedings have been issued for:%sCALLOUT",
                    System.lineSeparator()),
                "Next steps",
                "You should now check the order to see your directions and compliance dates.",
                "As the file exceeds the size limit that could not be sent by email you will need to download it from the Portal using this link.",
                String.format("http://fakeurl/%s", System.lineSeparator()),
                "HM Courts & Tribunals Service",
                "Do not reply to this email. If you need to contact us, call 0330 808 4424 or "
                    + "email contactfpl@justice.gov.uk")
        );
    }
}
