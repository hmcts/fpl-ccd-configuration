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
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.DocumentUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentUploadedNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.LA_USER;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithApplicationDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByHmtcs;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesBySolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildConfidentialDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildHearingFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildNonConfidentialPdfDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildRespondentStatementsList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildSubmittedCaseData;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyApplicationDocument;
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
class DocumentUploadedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String LA2_USER_EMAIL = "la2@examaple.com";
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
    private DocumentUploadedNotificationService documentUploadedNotificationService;

    @Mock
    private TranslationRequestService translationRequestService;

    @Mock
    private FurtherEvidenceUploadDifferenceCalculator calculator;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @InjectMocks
    private DocumentUploadedEventHandler documentUploadedEventHandler;

    @Captor
    private ArgumentCaptor<CourtBundleData> courtBundleCaptor;

    @Captor
    private ArgumentCaptor<NewDocumentData> newDocumentDataCaptor;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    private static final Consumer<CaseData> EMPTY_CASE_DATA_MODIFIER = whatever -> { };

    private void verifyNotificationFurtherDocumentsTemplate(final UserDetails uploadedBy,
                                                             DocumentUploaderType uploadedType,
                                                             Consumer<CaseData> beforeCaseDataModifier,
                                                             Consumer<CaseData> caseDataModifier,
                                                             List<String> expectedDocumentNames) {
        CaseData caseDataBefore = buildSubmittedCaseData();
        beforeCaseDataModifier.accept(caseDataBefore);
        CaseData caseData = buildSubmittedCaseData();
        caseDataModifier.accept(caseData);
        boolean isHavingNotification = expectedDocumentNames != null;

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                caseDataBefore,
                uploadedType,
                uploadedBy);

        if (isHavingNotification) {
            when(documentUploadedNotificationService.getRepresentativeEmails(caseData))
                .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
            when(documentUploadedNotificationService.getDesignatedLocalAuthorityRecipients(caseData))
                .thenReturn(Set.of(LA_USER_EMAIL));
            when(documentUploadedNotificationService.getLocalAuthoritiesRecipients(caseData))
                .thenReturn(Set.of(LA2_USER_EMAIL));
        }

        documentUploadedEventHandler.sendDocumentsUploadedNotification(documentUploadedEvent);

        if (isHavingNotification) {
            verify(documentUploadedNotificationService).sendNotification(
                caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL, LA2_USER_EMAIL), SENDER,
                expectedDocumentNames);
        } else {
            verify(documentUploadedNotificationService, never()).sendNotification(any(), any(), any(), any());
        }
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialApplicationDocumentIsUploadedByLA() {
        // Further documents for main application -> Further application documents
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getApplicationDocuments().addAll(
                wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, LA_USER, false,
                    PDF_DOCUMENT_1))),
            List.of(BIRTH_CERTIFICATE.getLabel()));
    }

    @Test
    void shouldSendNotificationWhenConfidentialApplicationDocumentIsUploadedByLA() {
        // Further documents for main application -> Further application documents
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getApplicationDocuments().addAll(
                wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, LA_USER, true,
                    PDF_DOCUMENT_1))), null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialRespondentStatementsIsUploadedByLA() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildNonConfidentialPdfDocumentList(LA_USER))),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialRespondentStatementsIsUploadedByLA() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildConfidentialDocumentList(LA_USER))),null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialAnyOtherDocIsUploadedByLA() {
        // Further documents for main application -> Any other document does not relate to a hearing
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(buildNonConfidentialPdfDocumentList(LA_USER)),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialAnyOtherDocIsUploadedByLA() {
        // Further documents for main application -> Any other document does not relate to a hearing
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(buildConfidentialDocumentList(LA_USER)), null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialAnyOtherDocRelatingToHearingIsUploadedByLA() {
        // Further documents for main application -> Any other document relates to a hearing
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getHearingFurtherEvidenceDocuments().addAll(
                buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(LA_USER))), NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialAnyOtherDocRelatingToHearingIsUploadedByLA() {
        // Further documents for main application -> Any other document relates to a hearing
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getHearingFurtherEvidenceDocuments().addAll(
                buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(LA_USER))), null);
    }

    @Test
    void shouldNotSendNotificationWhenAnyOtherDocsAreRemovedByLA() {
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(
                buildNonConfidentialPdfDocumentList(LA_USER)),
            EMPTY_CASE_DATA_MODIFIER, null);
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreSameByLA() {
        List<Element<SupportingEvidenceBundle>> documents =
            buildNonConfidentialPdfDocumentList(LA_USER);
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(documents),
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(documents), null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialRespondentStatementsIsUploadedByHMCTS() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildNonConfidentialPdfDocumentList(HMCTS_USER))),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialRespondentStatementsIsUploadedByHMCTS() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildConfidentialDocumentList(HMCTS_USER))),null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialAnyOtherDocIsUploadedByHMCTS() {
        // Further documents for main application -> Any other document
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(
                buildNonConfidentialPdfDocumentList(HMCTS_USER)),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialAnyOtherDocIsUploadedByHMCTS() {
        // Further documents for main application -> Any other document
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(
                buildConfidentialDocumentList(HMCTS_USER)), null);
    }

    @Test
    void shouldNotSendNotificationWhenAnyOtherDocsAreRemovedByHMCTS() {
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(
                buildNonConfidentialPdfDocumentList(HMCTS_USER)),
            EMPTY_CASE_DATA_MODIFIER, null);
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreSameByHMCTS() {
        List<Element<SupportingEvidenceBundle>> documents =
            buildNonConfidentialPdfDocumentList(HMCTS_USER);
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsHMCTS(), HMCTS,
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(documents),
            (caseData) ->  caseData.getFurtherEvidenceDocuments().addAll(documents), null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialAnyDocIsUploadedByRespSolicitor() {
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsRespondentSolicitor(), SOLICITOR, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocumentsSolicitor().addAll(
                buildNonConfidentialPdfDocumentList(REP_USER)),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialAnyDocIsUploadedByRespSolicitor() {
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsRespondentSolicitor(), SOLICITOR, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getFurtherEvidenceDocumentsSolicitor().addAll(
                buildConfidentialDocumentList(REP_USER)),
            null);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialRespondentStatementsIsUploadedByRespSolicitor() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsRespondentSolicitor(), SOLICITOR, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildNonConfidentialPdfDocumentList(REP_USER))),
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenConfidentialRespondentStatementsIsUploadedByRespSolicitor() {
        // Further documents for main application -> Respondent Statement
        verifyNotificationFurtherDocumentsTemplate(
            userDetailsRespondentSolicitor(), SOLICITOR, EMPTY_CASE_DATA_MODIFIER,
            (caseData) ->  caseData.getRespondentStatements().addAll(
                buildRespondentStatementsList(buildConfidentialDocumentList(REP_USER))),null);
    }

    @Test
    void shouldNotNotifyTranslationTeamWhenNoChange() {
        when(calculator.calculate(CASE_DATA, CASE_DATA_BEFORE)).thenReturn(List.of());

        documentUploadedEventHandler.notifyTranslationTeam(new DocumentUploadedEvent(CASE_DATA,
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

        documentUploadedEventHandler.notifyTranslationTeam(new DocumentUploadedEvent(CASE_DATA,
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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        documentUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        documentUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);
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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        documentUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);
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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseData,

                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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

        DocumentUploadedEvent documentUploadedEvent =
                new DocumentUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        documentUploadedEventHandler.sendDocumentsToCafcass(documentUploadedEvent);

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
