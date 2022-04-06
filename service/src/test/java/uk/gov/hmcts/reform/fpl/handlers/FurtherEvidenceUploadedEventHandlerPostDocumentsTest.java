package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.DocumentUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.REP_USER;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialNonPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialNonPdfDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsRespondentSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(MockitoExtension.class)
class  FurtherEvidenceUploadedEventHandlerPostDocumentsTest {

    private static final List<Recipient> RECIPIENTS_LIST = createRecipientsList();

    @Mock
    private SendDocumentService sendDocumentService;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Captor
    private ArgumentCaptor<CourtBundleData> courtBundleCaptor;

    @InjectMocks
    private DocumentUploadedEventHandler furtherEvidenceUploadedEventHandler;


    @Test
    void shouldSendDocumentByPostWhenPDFUploadedByRespSolicitor() {
        final CaseData caseData = buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(REP_USER);

        when(sendDocumentService.getStandardRecipients(caseData)).thenReturn(RECIPIENTS_LIST);

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsByPost(documentUploadedEvent);

        List<DocumentReference> documents = List.of(PDF_DOCUMENT_1, PDF_DOCUMENT_2);
        verify(sendDocumentService).sendDocuments(caseData, documents, RECIPIENTS_LIST);
    }


    @Test
    void shouldNotSendDocumentByPostWhenPDFUploadedByLA() {
        final CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsByPost(documentUploadedEvent);

        verify(sendDocumentService, never()).sendDocuments(any(), any(), any());
    }

    @Test
    void shouldNotSendDocumentByPostWhenPDFUploadedBySolicitor() {
        final CaseData caseData = buildCaseDataWithNonConfidentialNonPdfDocumentsSolicitor(REP_USER);

        when(sendDocumentService.getStandardRecipients(caseData)).thenReturn(RECIPIENTS_LIST);

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsByPost(documentUploadedEvent);

        verify(sendDocumentService).sendDocuments(caseData, new ArrayList<>(), RECIPIENTS_LIST);
    }

    @Test
    void shouldSendDocumentByPostWhenResponseStatementPdfIsUploadedByASolicitor() {
        final CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        when(sendDocumentService.getStandardRecipients(caseData)).thenReturn(RECIPIENTS_LIST);

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsByPost(documentUploadedEvent);

        List<DocumentReference> documents = List.of(PDF_DOCUMENT_1, PDF_DOCUMENT_2);
        verify(sendDocumentService).sendDocuments(caseData, documents, RECIPIENTS_LIST);
    }

    @Test
    void shouldRemoveNonPdfResponseStatements() {
        final CaseData caseData = buildCaseDataWithNonConfidentialNonPDFRespondentStatementsSolicitor();

        when(sendDocumentService.getStandardRecipients(caseData)).thenReturn(RECIPIENTS_LIST);

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER),
                SOLICITOR,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.sendDocumentsByPost(documentUploadedEvent);

        verify(sendDocumentService).sendDocuments(caseData, new ArrayList<>(), RECIPIENTS_LIST);
    }

    @Test
    void shouldEmailCafcassWhenNewBundleAdded() {
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
        CaseData caseDataBefore = commonCaseBuilder().build();

        DocumentUploadedEvent documentUploadedEvent =
            new DocumentUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);

        List<CourtBundle> courtBundles = unwrapElements(caseData.getCourtBundleList());
        Set<DocumentReference> documentReferences = courtBundles.stream()
            .map(CourtBundle::getDocument)
            .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
            eq(documentReferences),
            eq(COURT_BUNDLE),
            courtBundleCaptor.capture());

        CourtBundleData courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(hearing);
    }

    @Test
    void shouldNotEmailCafcassWhenNoNewBundle() {
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
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);

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
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);
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
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(documentUploadedEvent);
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

    private static List<Recipient> createRecipientsList() {
        final Representative representative = mock(Representative.class);
        final RespondentParty respondent = mock(RespondentParty.class);
        return List.of(representative, respondent);
    }
}
