package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithApplicationDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByHmtcs;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesBySolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsHMCTS;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsRespondentSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @InjectMocks
    private FurtherEvidenceUploadedEventHandler furtherEvidenceUploadedEventHandler;

    @Captor
    private ArgumentCaptor<CourtBundleData> courtBundleCaptor;

    @Captor
    private ArgumentCaptor<NewDocumentData> newDocumentDataCaptor;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, DESIGNATED_LOCAL_AUTHORITY))
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
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, DESIGNATED_LOCAL_AUTHORITY))
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
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
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
                DESIGNATED_LOCAL_AUTHORITY,
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
                DESIGNATED_LOCAL_AUTHORITY,
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
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByHMCTS() {
        CaseData caseData = buildCaseDataWithNonConfidentialDocuments(HMCTS_USER);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, HMCTS))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
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
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
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
    void shouldSendNotificationWhenNonConfidentialResponseStatementIsUploadedByRespSolicitor() {
        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, SOLICITOR))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService)
            .sendNotification(caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER,
                NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialResponseStatementIsUploadedByRespSolicitor() {
        // Not possible in journey but can happen in code.
        CaseData caseData = buildCaseDataWithConfidentialRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER),
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

    @Test
    void shouldNotEmailCafcassWhenNoNewBundle() {
        String hearing = "Hearing";
        CaseData caseData = buildCaseDataWithCourtBundleList(
                2,
                hearing,
                "LA");
        CaseData caseDataBefore = commonCaseBuilder()
                .courtBundleList(caseData.getCourtBundleList())
                .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
                any(),
                eq(COURT_BUNDLE),
                any());
    }

    @Test
    void shouldEmailCafcassWhenNewBundleIsAdded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );
        String hearing = "Hearing";
        CaseData caseData = buildCaseDataWithCourtBundleList(
                2,
                hearing,
                "LA");
        List<Element<CourtBundle>> courtBundleList = caseData.getCourtBundleList();
        Element<CourtBundle> existingBundle = courtBundleList.remove(1);

        CaseData caseDataBefore = commonCaseBuilder()
                .courtBundleList(List.of(existingBundle))
                .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);
        Set<DocumentReference> documentReferences = courtBundleList.stream()
                .map(courtBundle -> courtBundle.getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(documentReferences),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        CourtBundleData courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(hearing);
    }


    @Test
    void shouldEmailCafcassWhenNewBundlesAreAdded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );
        String hearing = "Hearing";
        String secHearing = "secHearing";
        String hearingOld = "Old";
        List<Element<CourtBundle>> hearing1 = createCourtBundleList(2, hearing, "LA");
        List<Element<CourtBundle>> oldHearing = createCourtBundleList(1, hearingOld, "LA");
        List<Element<CourtBundle>> hearing2 = createCourtBundleList(3, hearing, "LA");
        List<Element<CourtBundle>> secHearingBundle = createCourtBundleList(2, secHearing, "LA");

        List<Element<CourtBundle>> totalHearing = new ArrayList<>(hearing1);
        totalHearing.addAll(oldHearing);
        totalHearing.addAll(hearing2);
        totalHearing.addAll(secHearingBundle);

        Collections.shuffle(totalHearing);

        CaseData caseData = commonCaseBuilder()
                .courtBundleList(totalHearing)
                .build();

        CaseData caseDataBefore = commonCaseBuilder()
                .courtBundleList(oldHearing)
                .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);
        List<Element<CourtBundle>> expectedBundle = new ArrayList<>(hearing1);
        expectedBundle.addAll(hearing2);

        Set<DocumentReference> documentReferences = expectedBundle.stream()
                .map(courtBundle -> courtBundle.getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(documentReferences),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        CourtBundleData courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(hearing);

        Set<DocumentReference> secDocBundle = secHearingBundle.stream()
                .map(courtBundle -> courtBundle.getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(secDocBundle),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(secHearing);
    }

    @Test
    void shouldEmailCafcassWhenDocsIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getFurtherEvidenceDocumentsLA())
                .stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Child's guardian reports\n"
                        + "• Child's guardian reports");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldEmailCafcassWhenRespondentStatementIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getRespondentStatements())
                .stream()
                .flatMap(statement -> unwrapElements(statement.getSupportingEvidenceBundle()).stream())
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Respondent statement");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldEmailCafcassWhenApplicationDocumentIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithApplicationDocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getApplicationDocuments())
                .stream()
                .map(ApplicationDocument::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Birth certificate\n"
                        + "• Birth certificate");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldNotEmailCafcassWhenNoApplicationDocumentIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithApplicationDocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getApplicationDocuments())
                .stream()
                .map(ApplicationDocument::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldNotSendEmailToCafcassWhenRespondentStatementIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,

                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldNotSendEmailToCafcassWhenCafcassIsNotEnlang() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.empty()
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,

                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedByHmtcs() {
        CaseData caseData = buildCaseDataWithCorrespondencesByHmtcs();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocuments());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithCorrespondencesByLA();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocumentsLA());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedBySolicitor() {
        CaseData caseData = buildCaseDataWithCorrespondencesBySolicitor();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocumentsSolicitor());
    }

    private void verifyCorresspondences(CaseData caseData, List<Element<SupportingEvidenceBundle>> correspondence) {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(correspondence)
                .stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Correspondence");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Correspondence");
    }


    @Test
    void shouldNotSendEmailToCafcassWhenNoNewDocIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }


    private static List<String> buildNonConfidentialDocumentsNamesList() {
        return List.of(NON_CONFIDENTIAL_1, NON_CONFIDENTIAL_2);
    }

    private static List<String> buildConfidentialDocumentsNamesList() {
        return List.of(CONFIDENTIAL_1);
    }
}
