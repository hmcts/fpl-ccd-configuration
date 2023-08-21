package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.DocumentsHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.buildSubmittedCaseDataWithNewDocumentUploaded;
import static uk.gov.hmcts.reform.fpl.handlers.ManageDocumentsUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService.PDF;

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

    private static final UserDetails ADMIN_USER_DETAIL = UserDetails.builder()
        .id("ADMIN_USER_ID")
        .surname("Hudson")
        .forename("Steve")
        .email("steve.hudson@gov.uk")
        .roles(List.of("caseworker-publiclaw-courtadmin"))
        .build();

    private static final UserDetails SOLICITOR_USER_DETAIL = UserDetails.builder()
        .id("SOLICITOR_USER_ID")
        .surname("Hudson")
        .forename("Steve")
        .email("solicitor1@solicitors.uk")
        .roles(List.of("caseworker-publiclaw-solicitor"))
        .build();

    private static final List<Recipient> REPRESTATIVES_SERVED_BY_POST =
        List.of(Representative.builder().email(REP_SOLICITOR_1_EMAIL).build());


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

    @BeforeEach
    void setUp() {
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

        when(sendDocumentService.getStandardRecipients(any())).thenReturn(REPRESTATIVES_SERVED_BY_POST);

//        when(cafcassLookupConfiguration.getCafcassEngland(any())).thenReturn(
//            Optional.of(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));
    }

    void setUp_uploadedByLA() {
        when(userService.getUserDetails()).thenReturn(LA_USER_DETAIL);
        when(userService.getCaseRoles(ManageDocumentsUploadedEventTestData.CASE_ID)).thenReturn(Set.of(LASHARED));
    }

    void setUp_uploadedBySolicitor() {
        when(userService.getUserDetails()).thenReturn(SOLICITOR_USER_DETAIL);
        when(userService.getCaseRoles(ManageDocumentsUploadedEventTestData.CASE_ID)).thenReturn(Set.of(SOLICITOR));
    }

    @Nested
    class DocumentsUploadedTest {

        final Set<ConfidentialLevel> CTSC_ALLOWED = Set.of(ConfidentialLevel.CTSC);
        final Set<ConfidentialLevel> LA_ALLOWED = Set.of(ConfidentialLevel.LA, ConfidentialLevel.CTSC);
        final Set<ConfidentialLevel> NON_CONFIDENTIAL_ALLOWED = Set.of(ConfidentialLevel.NON_CONFIDENTIAL,
            ConfidentialLevel.LA,
            ConfidentialLevel.CTSC);

        final Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>>
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

        @ParameterizedTest
        @MethodSource("allDocumentsTypeParameters")
        void shouldSendNotificationBasedOnConfigurationWhenDocumentsUploaded(DocumentType documentType,
                                                                             ConfidentialLevel confidentialLevel)
            throws Exception {
            setUp_uploadedByLA();

            CaseData caseDataBefore = commonCaseBuilder().build();
            CaseData casedData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));

            ManageDocumentsUploadedEvent eventData =
                manageDocumentService.buildManageDocumentsUploadedEvent(casedData, caseDataBefore);

            underTest.sendDocumentsUploadedNotification(eventData);

            Map.of(NON_CONFIDENTIAL_ALLOWED, eventData.getNewDocuments(),
                    LA_ALLOWED, eventData.getNewDocumentsLA(),
                    CTSC_ALLOWED, eventData.getNewDocumentsCTSC())
                .forEach((levelAllowed, newDocuments) -> {
                    if (newDocuments != null && !newDocuments.isEmpty()) {
                        newDocuments.forEach((docType, documents) -> {
                            DocumentUploadedNotificationConfiguration config = docType.getNotificationConfiguration();

                            String senderName = eventData.getInitiatedBy().getFullName();
                            List<String> documentNames = documents.stream()
                                .map(Element::getValue)
                                .map(NotifyDocumentUploaded::getNameForNotification)
                                .collect(toList());

                            RECIPIENT_CONFIG_MAPPING.forEach((recipients, getConfigFunc) -> {
                                if (levelAllowed.contains(getConfigFunc.apply(config))) {
                                    verify(furtherEvidenceNotificationService).sendNotification(any(),
                                        eq(recipients), eq(senderName), eq(documentNames));
                                }
                            });
                        });
                    }
                });

            verifyNoMoreInteractions(furtherEvidenceNotificationService);
        }

        @ParameterizedTest
        @MethodSource("allDocumentsTypeParameters")
        void shouldSendDocumentToCafcassWhenDocumentUploaded(DocumentType documentType,
                                                             ConfidentialLevel confidentialLevel)
            throws Exception {
        }

        @ParameterizedTest
        @MethodSource("allDocumentsTypeParameters")
        void shouldNotifyTranslationTeamWhenDocumentUploaded(DocumentType documentType,
                                                             ConfidentialLevel confidentialLevel)
            throws Exception {
        }

        @ParameterizedTest
        @MethodSource("allDocumentsTypeParameters")
        void shouldSendByPostWhenSolicitorUploadedPdfDocument(DocumentType documentType,
                                                              ConfidentialLevel confidentialLevel)
            throws Exception {
            setUp_uploadedBySolicitor();

            CaseData caseDataBefore = commonCaseBuilder().build();
            CaseData casedData = buildSubmittedCaseDataWithNewDocumentUploaded(List.of(documentType),
                List.of(confidentialLevel));

            ManageDocumentsUploadedEvent eventData =
                manageDocumentService.buildManageDocumentsUploadedEvent(casedData, caseDataBefore);

            underTest.sendDocumentsByPost(eventData);

            if (ConfidentialLevel.NON_CONFIDENTIAL.equals(confidentialLevel)) {
                List<DocumentReference> expectedDocuments = eventData.getNewDocuments().values().stream()
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .map(NotifyDocumentUploaded::getDocument)
                    .filter(docRef -> DocumentsHelper.hasExtension(docRef.getFilename(), PDF))
                    .collect(toList());

                RECIPIENT_CONFIG_MAPPING.forEach((recipients, getConfigFunc) -> {
                    if (NON_CONFIDENTIAL_ALLOWED.contains(getConfigFunc.apply(documentType.getNotificationConfiguration()))) {
                        verify(sendDocumentService).sendDocuments(any(), eq(expectedDocuments),
                            eq(REPRESTATIVES_SERVED_BY_POST));
                    }
                });
            } else {
                verifyNoInteractions(sendDocumentService);
            }
        }

        @Test
        void shouldNotSendByPostWhenDocumentsAreNotUploadedBySolicitor() {
            setUp_uploadedByLA();

            ManageDocumentsUploadedEvent eventData = ManageDocumentsUploadedEvent.builder()
                .uploadedUserType(DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY).build();

            verifyNoInteractions(sendDocumentService);
        }

        private static Stream<Arguments> allDocumentsTypeParameters() {
            List<Arguments> streamList = new ArrayList<>();

            for (DocumentType docType : DocumentType.values()) {
                if (!docType.equals(DocumentType.COURT_BUNDLE) && !docType.equals(DocumentType.CASE_SUMMARY)
                    && !docType.equals(DocumentType.POSITION_STATEMENTS)
                    && !docType.equals(DocumentType.SKELETON_ARGUMENTS)) {
                    for(ConfidentialLevel level : ConfidentialLevel.values()) {
                        streamList.add(Arguments.of(docType, level));
                    }
                }
            }

            return streamList.stream();
        }
    }
}
