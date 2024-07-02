package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentMetadataDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CHANGE_OF_ADDRESS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
    private static final long CASE_ID = 12345L;
    private CaseData caseData;

    @Mock
    private EmailService emailService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private DocumentMetadataDownloadService documentMetadataDownloadService;
    @Mock
    private CafcassEmailConfiguration configuration;
    @Mock
    private CaseUrlService caseUrlService;
    @Mock
    private FeatureToggleService featureToggleService;

    private CafcassNotificationService underTest;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    @BeforeEach
    void setUp() {
        underTest = new CafcassNotificationService(
                emailService,
                documentDownloadService,
                configuration,
                caseUrlService,
                documentMetadataDownloadService,
                25L,
                featureToggleService
        );
        Child william = Child.builder().party(
                        ChildParty.builder()
                                .dateOfBirth(LocalDate.of(2015, 12, 22))
                                .lastName("William")
                                .build())
                .build();


        Child hares = Child.builder().party(ChildParty.builder()
                        .dateOfBirth(LocalDate.of(2017, 4, 10))
                        .lastName("Hares")
                        .build())
                .build();

        caseData = CaseData.builder()
                .id(CASE_ID)
                .children1(List.of(element(william), element(hares)))
                .familyManCaseNumber(FAMILY_MAN)
                .build();

        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentMetadataDownloadService.getDocumentMetadata(anyString()))
            .thenReturn(DocumentReference.builder()
                    .url("originalDocumentUrl")
                    .filename("fileToSend.pdf")
                    .binaryUrl("originalDocumentBinaryUrl")
                    .size(10L)
                    .build());
        when(configuration.getDocumentType()).thenReturn(
            ofEntries(
                entry("order", "ORDER"),
                entry("noticeOfHearing", "ORDER"),
                entry("respondentStatement", "COURT PAPER"),
                entry("applicationStatement", "COURT PAPER"),
                entry("otherReports", "COURT PAPER"),
                entry("threshold", "COURT PAPER"),
                entry("swet", "COURT PAPER"),
                entry("carePlan", "COURT PAPER"),
                entry("socialWorkChronology", "COURT PAPER"),
                entry("socialWorkStatement", "COURT PAPER"),
                entry("genogram", "COURT PAPER"),
                entry("checklistDocument", "COURT PAPER"),
                entry("birthCertificate", "COURT PAPER"),
                entry("additionalApplications", "COURT PAPER"),
                entry("expertReports", "EXPERT"),
                entry("childsGuardianReports", "REPORTING TO COURT"),
                entry("courtBundle", "BUNDLE"),
                entry("other", "CORRESPONDENCE"),
                entry("correspondence", "CORRESPONDENCE"),
                entry("additionalDocument", "APPLICATION"),
                entry("newApplication", "APPLICATION")

            ));
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|ORDER|18/05/2022", "false, Court Ref. FM1234.- new order"})
    void shouldNotifyOrderRequest(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForOrder()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
            DOCUMENT_CONTENT);

        underTest.sendEmail(caseData,
            of(getDocumentReference()),
            ORDER,
            OrderCafcassData.builder()
                .documentName(TITLE)
                .orderApprovalDate(LocalDate.of(2022, 5, 18))
                .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
            String.join(" ",
                "A new order for this case was uploaded to the Public Law Portal entitled",
                TITLE)
        );
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|ORDER|16/05/2022 10:30|18/05/2022", "false, Court Ref. FM1234.- new order"})
    void shouldNotifyOrderRequestWithHearingSelected(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForOrder()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        LocalDate orderDate = LocalDate.of(2022, 5, 18);
        LocalDateTime hearingDateTime = LocalDateTime.of(orderDate.minusDays(2),
                LocalTime.of(10, 30));

        underTest.sendEmail(caseData,
                of(getDocumentReference()),
                ORDER,
                OrderCafcassData.builder()
                        .documentName(TITLE)
                        .orderApprovalDate(orderDate)
                        .hearingDate(hearingDateTime)
                        .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "A new order for this case was uploaded to the Public Law Portal entitled",
                        TITLE)
        );
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|APPLICATION",
        "false, 'Urgent application – same day hearing, Oliver Wright'"})
    void shouldNotifyUrgentNewApplicationRequest(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
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


        underTest.sendEmail(caseData,
            of(getDocumentReference().toBuilder()
                    .type(NEW_APPLICATION.getLabel())
                    .build()),
            NEW_APPLICATION,
            newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(
            subject);

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

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|APPLICATION","false, 'Application received, Oliver Wright'"})
    void shouldNotifyNewApplicationRequestWhenNoTimeFramePresent(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
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

        underTest.sendEmail(caseData,
                of(getDocumentReference().toBuilder()
                        .type(NEW_APPLICATION.getLabel())
                        .build()),
                NEW_APPLICATION,
                newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);

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

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|APPLICATION",
        "false, 'Application received – hearing within 7 days, Oliver Wright'"})
    void shouldNotifyNonUrgentNewApplicationRequest(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
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

        underTest.sendEmail(caseData,
                of(getDocumentReference().toBuilder()
                        .type(NEW_APPLICATION.getLabel())
                        .build()),
                NEW_APPLICATION,
                newApplicationCafcassData
        );


        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);
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

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|BUNDLE", "false, 'Court Ref. FM1234.- new court bundle'"})
    void shouldNotifyCourtBundle(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForCourtBundle()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

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
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "A new court bundle for this case was uploaded to the Public Law Portal entitled",
                        TITLE)
        );
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|REPORTING TO COURT",
        "false, 'Court Ref. FM1234.- Further documents for main application'"})
    void shouldNotifyNewDocument(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForNewDocument()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        underTest.sendEmail(caseData,
                of(getDocumentReference().toBuilder()
                    .type("Child's Guardian Reports")
                    .build()
                ),
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
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "Types of documents attached:\n\n"
                                + "• Application statement")
        );
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|COURT PAPER",
        "false, 'Court Ref. FM1234.- Further documents for main application'"})
    void shouldNotifyDuplicateNewDocumentsAreUploaded(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForNewDocument()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        List<DocumentReference> documentReference = List.of(
                getDocumentReference().toBuilder()
                        .type("Respondent Statement")
                        .build(),
                getDocumentReference().toBuilder()
                        .type("SWET")
                        .build());

        Set<DocumentReference> documentReferences = Set.copyOf(documentReference);
        assertThat(documentReferences).hasSize(2);

        underTest.sendEmail(caseData,
                documentReferences,
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
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "Types of documents attached:\n\n"
                                + "• Application statement")
        );
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|EXPERT","false, 'Court Ref. FM1234.- additional documents'"})
    void shouldNotifyAdditionalDocument(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForAdditionlDocument()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

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
        assertThat(data.getSubject()).isEqualTo(subject);
        assertThat(data.getAttachments()).containsExactly(
                document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
                String.join(" ",
                        "Types of documents attached:\n\n"
                                + "• Additional statement")
        );
    }

    @ParameterizedTest
    @CsvSource({"true, 'Court Ref. FM1234.- '", "false, 'Court Ref. FM1234.- '"})
    void shouldNotifyAdditionalDocumentWithBlankSujbectWhenNoDocumentsPresent(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForAdditionlDocument()).thenReturn(RECIPIENT_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);

        underTest.sendEmail(caseData,
                of(),
                ADDITIONAL_DOCUMENT,
                NewDocumentData.builder()
                        .emailSubjectInfo("")
                        .build()
        );

        verify(documentDownloadService, never()).downloadDocument(any());

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);
    }

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|ORDER|20/04/2050 11:30",
        "false, 'Court Ref. FM1234.- New case management hearing Oliver Wright - notice of hearing'"})
    void shouldNotifyNoficeOfHearing(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        when(configuration.getRecipientForNoticeOfHearing()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
                DOCUMENT_CONTENT);
        LocalDate hearingDate = LocalDate.of(2050, Month.APRIL,20);
        LocalDateTime hearingDateTime = LocalDateTime.of(hearingDate, LocalTime.of(11,30));
        String hearingVenue = "London";
        String hearingHearingTime = "1 hour before the hearing";
        String hearingTime = "18 June, 3:40pm - 19 June, 2:30pm";


        underTest.sendEmail(caseData,
            of(getDocumentReference()),
            NOTICE_OF_HEARING,
            NoticeOfHearingCafcassData.builder()
                    .hearingType(CASE_MANAGEMENT.getLabel().toLowerCase())
                    .firstRespondentName("James Wright")
                    .eldestChildLastName("Oliver Wright")
                    .hearingDate(hearingDateTime)
                    .hearingVenue(hearingVenue)
                    .preHearingTime(hearingHearingTime)
                    .hearingTime(hearingTime)
                    .build()
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());
        EmailData data = emailDataArgumentCaptor.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(subject);

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
                .thenReturn(DocumentReference.builder()
                        .filename(DOCUMENT_FILENAME)
                        .size(Long.MAX_VALUE)
                        .url(DOCUMENT_URL)
                        .build());


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
        assertThat(data.getSubject()).isEqualTo("Court Ref. FM1234.- new large document added - Expert reports");
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

    @ParameterizedTest
    @CsvSource({"true, William|FM1234|12345|EXPERT", "false, 'Court Ref. FM1234.- additional documents'"})
    void shouldNotifyWithAttachmentAndLinkWhenThereIsSmallAndLargeDocs(boolean flag, String subject) {
        when(featureToggleService.isCafcassSubjectCategorised()).thenReturn(flag);
        String caseLink = "http://localhost:8080/cases/case-details/200";
        String smallDocumentUrl = "smallDocumentUrl";

        when(configuration.getRecipientForAdditionlDocument()).thenReturn("additionalEmail");
        when(configuration.getRecipientForLargeAttachements()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(caseUrlService.getCaseUrl(CASE_ID)).thenReturn(caseLink);
        when(documentMetadataDownloadService.getDocumentMetadata(DOCUMENT_URL))
                .thenReturn(DocumentReference.builder()
                        .filename(DOCUMENT_FILENAME)
                        .size(Long.MAX_VALUE - 100000)
                        .url(DOCUMENT_URL)
                        .build());

        when(documentMetadataDownloadService.getDocumentMetadata(smallDocumentUrl))
                .thenReturn(DocumentReference.builder()
                        .filename("small.pdf")
                        .size(10L)
                        .url(smallDocumentUrl)
                        .binaryUrl(smallDocumentUrl)
                        .build());

        when(documentDownloadService.downloadDocument(smallDocumentUrl)).thenReturn(
                DOCUMENT_CONTENT);

        DocumentReference smallDoc = getDocumentReference().toBuilder()
                .url(smallDocumentUrl)
                .build();

        underTest.sendEmail(caseData,
                of(getDocumentReference(), smallDoc),
                ADDITIONAL_DOCUMENT,
                NewDocumentData.builder()
                        .documentTypes("• Additional statement\n • Large statement")
                        .emailSubjectInfo("additional documents")
                        .build()
        );

        verify(documentDownloadService).downloadDocument(smallDocumentUrl);
        verify(documentDownloadService, never()).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService, times(2)).sendEmail(eq(SENDER_EMAIL), emailDataArgumentCaptor.capture());

        List<EmailData> emailDataList = emailDataArgumentCaptor.getAllValues();

        String largeDocMessage = String.join("", "Large document(s) for this case was uploaded to the ",
                "Public Law Portal entitled fileToSend.pdf. As this could ",
                "not be sent by email you will need to download it ",
                "from the Portal using this link.",
                System.lineSeparator(),
                "http://localhost:8080/cases/case-details/200");

        assertThat(emailDataList)
                .extracting("recipient", "subject", "message")
                .containsExactlyInAnyOrder(
                        tuple("additionalEmail",
                                subject,
                                "Document attached is : small.pdf"),
                        tuple(RECIPIENT_EMAIL,
                                "Court Ref. FM1234.- new large document added - Expert reports",
                                largeDocMessage));
    }

    private DocumentReference getDocumentReference() {
        return DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .url(DOCUMENT_URL)
                .filename(DOCUMENT_FILENAME)
                .type("Expert reports")
                .build();
    }

}
