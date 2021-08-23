package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsHMCTS;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsRespondentSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceUploadedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    private static final String LA_USER = "LA";
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String HMCTS_USER_EMAIL = "hmcts@examaple.com";
    private static final String REP_SOLICITOR_USER_EMAIL = "rep@examaple.com";
    private static final String SENDER_FORENAME = "The";
    private static final String SENDER_SURNAME = "Sender";
    private static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final CaseData CASE_DATA_BEFORE = mock(CaseData.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;
    private static final DocumentReference DOCUMENT = mock(DocumentReference.class);
    private static final List<String> NON_CONFIDENTIAL = buildNonConfidentialDocumentsNamesList();
    private static final List<String> CONFIDENTIAL = buildConfidentialDocumentsNamesList();

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Mock
    private TranslationRequestService translationRequestService;

    @Mock
    private FurtherEvidenceUploadDifferenceCalculator calculator;

    @InjectMocks
    private FurtherEvidenceUploadedEventHandler furtherEvidenceUploadedEventHandler;

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                LOCAL_AUTHORITY,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, LOCAL_AUTHORITY))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL), SENDER,
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByLA() {
        CaseData caseDataBefore = buildCaseDataWithConfidentialLADocuments();

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        CaseData caseData = commonCaseBuilder().furtherEvidenceDocumentsLA(
            wrapElements(createDummyEvidenceBundle(CONFIDENTIAL_1, LA_USER, false, PDF_DOCUMENT_1))
        ).build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                LOCAL_AUTHORITY,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, LOCAL_AUTHORITY))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL), SENDER, CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByHMCTS() {
        CaseData caseDataBefore = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        CaseData caseData = commonCaseBuilder().furtherEvidenceDocuments(
            wrapElements(createDummyEvidenceBundle(CONFIDENTIAL_1, HMCTS_USER, false, PDF_DOCUMENT_1))
        ).build();

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, HMCTS))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                HMCTS,
                userDetailsHMCTS());
        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER,
            CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithConfidentialLADocuments();
        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreRemoved() {
        CaseData caseData = commonCaseBuilder().build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreSame() {
        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseData,
                LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByHMCTS() {
        CaseData caseData = buildCaseDataWithNonConfidentialDocuments(HMCTS_USER);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, HMCTS))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(HMCTS_USER),
                HMCTS,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER, NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByHMCTS() {
        CaseData caseData = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(HMCTS_USER),
                HMCTS,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByRespSolicitor() {
        CaseData caseData = buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(REP_USER);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, SOLICITOR))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService)
            .sendNotification(caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER,
                NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByRespSolicitor() {
        CaseData caseData = buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER);
        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }
    @Test
    void shouldNotNotifyTranslationTeamWhenNoChange() {
        when(calculator.calculate(CASE_DATA, CASE_DATA_BEFORE)).thenReturn(List.of());

        furtherEvidenceUploadedEventHandler.notifyTranslationTeam(new FurtherEvidenceUploadedEvent(CASE_DATA,
            CASE_DATA_BEFORE,
            null,
            null)
        );

        verifyNoInteractions(translationRequestService);
    }

    @Test
    void shouldNotifyTranslationTeamWhenChanges() {
        when(calculator.calculate(CASE_DATA, CASE_DATA_BEFORE)).thenReturn(List.of(
            element(UUID.randomUUID(), SupportingEvidenceBundle.builder()
                .type(FurtherEvidenceType.APPLICANT_STATEMENT)
                .name("Name")
                .dateTimeUploaded(LocalDateTime.of(2012, 1, 2, 3, 4, 5))
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .document(DOCUMENT)
                .build())
        ));

        furtherEvidenceUploadedEventHandler.notifyTranslationTeam(new FurtherEvidenceUploadedEvent(CASE_DATA,
            CASE_DATA_BEFORE,
            null,
            null)
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            DOCUMENT,
            "Application statement - Name - 2 January 2012");
        verifyNoMoreInteractions(translationRequestService);
    }

    private static List<String> buildNonConfidentialDocumentsNamesList() {
        return List.of(NON_CONFIDENTIAL_1, NON_CONFIDENTIAL_2);
    }

    private static List<String> buildConfidentialDocumentsNamesList() {
        return List.of(CONFIDENTIAL_1);
    }
}
