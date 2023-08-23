package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.utils.DocumentsHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.CTSC_ALLOWED;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.LA_ALLOWED;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.NON_CONFIDENTIAL_ALLOWED;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.buildSubmittedCaseDataWithNewDocumentUploaded;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.isHearingDocument;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService.PDF;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ManageDocumentsUploadedEventHandlerTest {

    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String LA2_USER_EMAIL = "la2@examaple.com";
    private static final String LEGAL_REPRESENTATIVE_EMAIL = "leagalRep@examaple.com";
    private static final String CAFCASS_REPRESENTATIVE_EMAIL = "cafcass@examaple.com";
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final String REP_SOLICITOR_3_EMAIL = "rep_solicitor3@example.com";
    private static final String CHILD_SOLICITOR_1_EMAIL = "child_solicitor1@example.com";
    private static final String CHILD_SOLICITOR_2_EMAIL = "child_solicitor2@example.com";
    private static final String CHILD_SOLICITOR_3_EMAIL = "child_solicitor3@example.com";

    private static final Set<String> DESIGNATED_LA_RECIPIENTS = Set.of(LA_USER_EMAIL);
    private static final Set<String> SECONDARY_LA_RECIPIENTS = Set.of(LA2_USER_EMAIL);
    private static final Set<String> LEGAL_REPRESENTATIVE_RECIPIENTS = Set.of(LEGAL_REPRESENTATIVE_EMAIL);
    private static final Set<String> CAFCASS_REPRESENTATIVE_RECIPIENTS = Set.of(CAFCASS_REPRESENTATIVE_EMAIL);
    private static final Set<String> REP_SOLICITOR_RECIPIENTS = Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL,
        REP_SOLICITOR_3_EMAIL);
    private static final Set<String> CHILD_SOLICITOR_RECIPIENTS = Set.of(CHILD_SOLICITOR_1_EMAIL,
        CHILD_SOLICITOR_2_EMAIL, CHILD_SOLICITOR_3_EMAIL);

    private static final UserDetails LA_USER_DETAIL = UserDetails.builder()
        .id("LA_USER_ID")
        .surname("Swansea")
        .forename("Kurt")
        .email("kurt@swansea.gov.uk")
        .roles(List.of("caseworker-publiclaw-solicitor"))
        .build();

    private static final UserDetails SOLICITOR_USER_DETAIL = UserDetails.builder()
        .id("SOLICITOR_USER_ID")
        .surname("Hudson")
        .forename("Steve")
        .email("solicitor1@solicitors.uk")
        .roles(List.of("caseworker-publiclaw-solicitor"))
        .build();

    private static final List<Recipient> REPRESENTATIVES_SERVED_BY_POST =
        List.of(Representative.builder().email(REP_SOLICITOR_1_EMAIL).build());

    private static final Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>>
        RECIPIENT_CONFIG_MAPPING =
        Map.of(
            DESIGNATED_LA_RECIPIENTS, DocumentUploadedNotificationConfiguration::getSendToDesignatedLA,
            SECONDARY_LA_RECIPIENTS, DocumentUploadedNotificationConfiguration::getSendToSecondaryLA,
            LEGAL_REPRESENTATIVE_RECIPIENTS,
            DocumentUploadedNotificationConfiguration::getSendToLegalRepresentative,
            CAFCASS_REPRESENTATIVE_RECIPIENTS,
            DocumentUploadedNotificationConfiguration::getSendToCafcassRepresentative,
            REP_SOLICITOR_RECIPIENTS, DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor,
            CHILD_SOLICITOR_RECIPIENTS, DocumentUploadedNotificationConfiguration::getSendToChildSolicitor
        );

    @InjectMocks
    private ManageDocumentsUploadedEventHandler underTest;

    @InjectMocks
    private ManageDocumentService manageDocumentService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Mock
    private SendDocumentService sendDocumentService;

    @Mock
    private UserService userService;

    @Mock
    private TranslationRequestService translationRequestService;

    @BeforeEach
    void setUp() {
        setUp_uploadedByLA();

        when(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(any()))
            .thenReturn(DESIGNATED_LA_RECIPIENTS);

        when(furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(any()))
            .thenReturn(SECONDARY_LA_RECIPIENTS);

        when(furtherEvidenceNotificationService.getLegalRepresentativeOnly(any()))
            .thenReturn(LEGAL_REPRESENTATIVE_RECIPIENTS);

        when(furtherEvidenceNotificationService.getCafcassRepresentativeEmails(any()))
            .thenReturn(CAFCASS_REPRESENTATIVE_RECIPIENTS);

        when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(any()))
            .thenReturn(REP_SOLICITOR_RECIPIENTS);

        when(furtherEvidenceNotificationService.getChildSolicitorEmails(any()))
            .thenReturn(CHILD_SOLICITOR_RECIPIENTS);

        when(sendDocumentService.getStandardRecipients(any())).thenReturn(REPRESENTATIVES_SERVED_BY_POST);

        when(cafcassLookupConfiguration.getCafcassEngland(any())).thenReturn(
            Optional.of(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));
    }

    void setUp_uploadedByLA() {
        when(userService.getUserDetails()).thenReturn(LA_USER_DETAIL);
        when(userService.getCaseRoles(ManageDocumentsUploadedEventTestData.CASE_ID)).thenReturn(Set.of(LASHARED));
    }

    void setUp_uploadedBySolicitor() {
        when(userService.getUserDetails()).thenReturn(SOLICITOR_USER_DETAIL);
        when(userService.getCaseRoles(ManageDocumentsUploadedEventTestData.CASE_ID)).thenReturn(Set.of(SOLICITOR));
    }

    @ParameterizedTest
    @MethodSource("allDocumentsTypeParameters")
    void shouldSendNotificationBasedOnConfigurationWhenDocumentsUploaded(DocumentType documentType,
                                                                         ConfidentialLevel confidentialLevel)
        throws Exception {

        CaseData caseDataBefore = commonCaseBuilder().build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));
        } catch (Exception e) {
            return;
        }

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.sendDocumentsUploadedNotification(eventData);


        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocuments;
        Set<ConfidentialLevel> levelAllowed;

        if (ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)) {
            expectedNewDocuments = eventData.getNewDocuments();
            levelAllowed = NON_CONFIDENTIAL_ALLOWED;
        } else if (ConfidentialLevel.LA.equals(confidentialLevel)) {
            expectedNewDocuments = eventData.getNewDocumentsLA();
            levelAllowed = LA_ALLOWED;
        } else {
            expectedNewDocuments = eventData.getNewDocumentsCTSC();
            levelAllowed = CTSC_ALLOWED;
        }

        List<Element<NotifyDocumentUploaded>> documents = expectedNewDocuments.get(documentType);
        List<String> documentNames = unwrapElements(documents).stream()
            .map(NotifyDocumentUploaded::getNameForNotification)
            .collect(toList());

        String senderName = eventData.getInitiatedBy().getFullName();


        verifyFurtherEvidenceNotificationServiceGetRecipients();

        DocumentUploadedNotificationConfiguration config = documentType.getNotificationConfiguration();

        if (config != null) {
            RECIPIENT_CONFIG_MAPPING.forEach((recipients, getConfigFunc) -> {
                ConfidentialLevel levelConfig = getConfigFunc.apply(config);
                if (levelConfig != null && levelAllowed.contains(levelConfig)) {
                    verify(furtherEvidenceNotificationService).sendNotification(any(),
                        eq(recipients), eq(senderName), eq(documentNames));
                }
            });
        }

        verifyNoMoreInteractions(furtherEvidenceNotificationService);
    }

    @ParameterizedTest
    @MethodSource("allDocumentsTypeParameters")
    void shouldSendDocumentToCafcassWhenDocumentUploaded(DocumentType documentType,
                                                         ConfidentialLevel confidentialLevel)
        throws Exception {
        CaseData caseDataBefore = commonCaseBuilder().build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));
        } catch (Exception e) {
            return;
        }

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.sendDocumentsToCafcass(eventData);

        if (documentType.getNotificationConfiguration() != null) {
            ConfidentialLevel cafcassConfidentialLevel = documentType.getNotificationConfiguration()
                .getSendToCafcassEngland();

            if (cafcassConfidentialLevel != null) {
                Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocuments = null;
                boolean shouldVerifyNoInteractionOnly = true;

                if (ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)) {
                    if (NON_CONFIDENTIAL_ALLOWED.contains(cafcassConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocuments();
                        shouldVerifyNoInteractionOnly = false;
                    }
                } else if (ConfidentialLevel.LA.equals(confidentialLevel)) {
                    if (LA_ALLOWED.contains(cafcassConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocumentsLA();
                        shouldVerifyNoInteractionOnly = false;
                    }
                } else {
                    if (CTSC_ALLOWED.contains(cafcassConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocumentsCTSC();
                        shouldVerifyNoInteractionOnly = false;
                    }
                }

                if (!shouldVerifyNoInteractionOnly) {
                    String expectedCafcassDocType = documentType.toString().replaceAll("_", " ");

                    Set<DocumentReference> expectedDocRef =
                        unwrapElements(expectedNewDocuments.get(documentType)).stream()
                            .map(NotifyDocumentUploaded::getDocument)
                            .map(documentReference ->
                                documentReference.toBuilder()
                                    .type(expectedCafcassDocType)
                                    .build())
                            .collect(toSet());


                    verify(cafcassNotificationService).sendEmail(any(),
                        eq(expectedDocRef),
                        eq(documentType.getNotificationConfiguration().getCafcassRequestEmailContentProvider()),
                        eq(NewDocumentData.builder()
                            .documentTypes("• " + documentType.getDescription())
                            .emailSubjectInfo((COURT_CORRESPONDENCE.equals(documentType))
                                ? ManageDocumentsUploadedEventHandler.CORRESPONDENCE
                                : ManageDocumentsUploadedEventHandler.FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                            .build()));
                }
            }
        }
        verifyNoMoreInteractions(translationRequestService);
    }

    @ParameterizedTest
    @MethodSource("allDocumentsTypeParameters")
    void shouldNotifyTranslationTeamWhenDocumentUploaded(DocumentType documentType,
                                                         ConfidentialLevel confidentialLevel)
        throws Exception {
        CaseData caseDataBefore = commonCaseBuilder().languageRequirement(YesNo.YES.getValue()).build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));
        } catch (Exception e) {
            return;
        }
        caseData = caseData.toBuilder().languageRequirement(YesNo.YES.getValue()).build();

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.notifyTranslationTeam(eventData);

        if (documentType.getNotificationConfiguration() != null) {
            ConfidentialLevel translationConfidentialLevel = documentType.getNotificationConfiguration()
                .getSendToTranslationTeam();

            if (translationConfidentialLevel != null) {
                Map<DocumentType, List<Element<NotifyDocumentUploaded>>> expectedNewDocuments = null;
                boolean shouldVerifyNoInteractionOnly = true;

                if (ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)) {
                    if (NON_CONFIDENTIAL_ALLOWED.contains(translationConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocuments();
                        shouldVerifyNoInteractionOnly = false;
                    }
                } else if (ConfidentialLevel.LA.equals(confidentialLevel)) {
                    if (LA_ALLOWED.contains(translationConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocumentsLA();
                        shouldVerifyNoInteractionOnly = false;
                    }
                } else {
                    if (CTSC_ALLOWED.contains(translationConfidentialLevel)) {
                        expectedNewDocuments = eventData.getNewDocumentsCTSC();
                        shouldVerifyNoInteractionOnly = false;
                    }
                }

                if (!shouldVerifyNoInteractionOnly) {
                    unwrapElements(expectedNewDocuments.get(documentType)).forEach(doc ->
                        verify(translationRequestService).sendRequest(any(),
                            eq(Optional.of(LanguageTranslationRequirement.ENGLISH_TO_WELSH)),
                            eq(doc.getDocument()),
                            any()));
                }
            }
        }
        verifyNoMoreInteractions(translationRequestService);
    }

    @Test
    void shouldNotNotifyTranslationTeamIfTranslationIsNotRequiredForTheCase()
        throws Exception {
        CaseData caseDataBefore = commonCaseBuilder().languageRequirement(YesNo.NO.getValue()).build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(DocumentType.EXPERT_REPORTS),
                List.of(ConfidentialLevel.NON_CONFIDENTIAL));
        } catch (Exception e) {
            return;
        }
        caseData = caseData.toBuilder().languageRequirement(YesNo.NO.getValue()).build();

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.notifyTranslationTeam(eventData);

        verifyNoMoreInteractions(translationRequestService);
    }

    @ParameterizedTest
    @MethodSource("allDocumentsTypeParameters")
    void shouldSendByPostWhenSolicitorUploadedPdfDocument(DocumentType documentType,
                                                          ConfidentialLevel confidentialLevel)
        throws Exception {
        setUp_uploadedBySolicitor();

        CaseData caseDataBefore = commonCaseBuilder().build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));
        } catch (Exception e) {
            return;
        }

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.sendDocumentsByPost(eventData);

        if (!isHearingDocument(documentType) && ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)
            && documentType.getNotificationConfiguration() != null) {

            List<DocumentReference> expectedDocuments = eventData.getNewDocuments().values().stream()
                .flatMap(List::stream)
                .map(Element::getValue)
                .map(NotifyDocumentUploaded::getDocument)
                .filter(docRef -> DocumentsHelper.hasExtension(docRef.getFilename(), PDF))
                .collect(toList());

            RECIPIENT_CONFIG_MAPPING.forEach((recipients, getConfigFunc) -> {
                ConfidentialLevel levelConfig = getConfigFunc.apply(documentType.getNotificationConfiguration());
                if (levelConfig != null && NON_CONFIDENTIAL_ALLOWED.contains(levelConfig)) {
                    verify(sendDocumentService).sendDocuments(any(), eq(expectedDocuments),
                        eq(REPRESENTATIVES_SERVED_BY_POST));
                }
            });
        } else {
            verifyNoInteractions(sendDocumentService);
        }
    }

    @Test
    void shouldNotSendByPostWhenDocumentsAreNotUploadedBySolicitor() {
        ManageDocumentsUploadedEvent eventData = ManageDocumentsUploadedEvent.builder()
            .uploadedUserType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY).build();

        underTest.sendDocumentsByPost(eventData);

        verifyNoInteractions(sendDocumentService);
    }

    @ParameterizedTest
    @MethodSource("allHearingDocumentsTypeParameters")
    void shouldNotNotifyTranslationTeamWhenHearingDocumentUploaded(DocumentType documentType,
                                                                   ConfidentialLevel confidentialLevel)
        throws Exception {
        // TODO
    }

    @ParameterizedTest
    @MethodSource("allHearingDocumentsTypeParameters")
    void shouldNotSendDocumentByPostWhenHearingDocumentUploaded(DocumentType documentType,
                                                                ConfidentialLevel confidentialLevel)
        throws Exception {
        setUp_uploadedBySolicitor();

        CaseData caseDataBefore = commonCaseBuilder().build();
        CaseData caseData;
        try {
            caseData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));
        } catch (Exception e) {
            return;
        }

        ManageDocumentsUploadedEvent eventData =
            manageDocumentService.buildManageDocumentsUploadedEvent(caseData, caseDataBefore);

        underTest.sendDocumentsByPost(eventData);

        verifyNoInteractions(sendDocumentService);
    }

    private static Stream<Arguments> allDocumentsTypeParameters() {
        List<Arguments> streamList = new ArrayList<>();

        for (DocumentType docType : DocumentType.values()) {
            for (ConfidentialLevel level : ConfidentialLevel.values()) {
                streamList.add(Arguments.of(docType, level));
            }
        }

        return streamList.stream();
    }

    private static Stream<Arguments> allHearingDocumentsTypeParameters() {
        List<Arguments> streamList = new ArrayList<>();

        Stream.of(DocumentType.COURT_BUNDLE, DocumentType.CASE_SUMMARY, DocumentType.POSITION_STATEMENTS,
                DocumentType.SKELETON_ARGUMENTS)
            .forEach(docType -> {
                for (ConfidentialLevel level : ConfidentialLevel.values()) {
                    streamList.add(Arguments.of(docType, level));
                }
            });

        return streamList.stream();
    }

    private void verifyFurtherEvidenceNotificationServiceGetRecipients() {
        verify(furtherEvidenceNotificationService).getDesignatedLocalAuthorityRecipientsOnly(any());
        verify(furtherEvidenceNotificationService).getSecondaryLocalAuthorityRecipientsOnly(any());
        verify(furtherEvidenceNotificationService).getLegalRepresentativeOnly(any());
        verify(furtherEvidenceNotificationService).getCafcassRepresentativeEmails(any());
        verify(furtherEvidenceNotificationService).getRespondentSolicitorEmails(any());
        verify(furtherEvidenceNotificationService).getChildSolicitorEmails(any());
    }
}

