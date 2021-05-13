package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.HearingBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.OtherDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atMost;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.LA;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT2;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DocumentListServiceTest {

    @Mock
    private RespondentStatementsTransformer respondentStatementsTransformer;

    @Mock
    private HearingBundleTransformer hearingBundleTransformer;

    @Mock
    private ApplicationDocumentBundleTransformer applicationDocumentTransformer;

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceTransformer;

    @Mock
    private OtherDocumentsTransformer otherDocumentsTransformer;

    @Mock
    private DocumentsListRenderer documentsListRenderer;

    @InjectMocks
    private DocumentListService documentListService;

    @Test
    void shouldRenderApplicationDocumentsBundleView() {
        List<Element<ApplicationDocument>> applicationDocuments = buildApplicationDocuments();
        List<Element<SupportingEvidenceBundle>> hearingEvidenceDocuments = List.of(ADMIN_NON_CONFIDENTIAL_DOCUMENT2);
        List<Element<SupportingEvidenceBundle>> furtherEvidenceHMCTS = List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT);

        List<Element<SupportingEvidenceBundle>> furtherEvidenceLA = List.of(
            LA_CONFIDENTIAL_DOCUMENT,
            LA_NON_CONFIDENTIAL_DOCUMENT);

        UUID hearingId = UUID.randomUUID();
        List<Element<HearingFurtherEvidenceBundle>> hearingEvidence = wrapElements(
            HearingFurtherEvidenceBundle.builder()
                .hearingName("hearing1")
                .supportingEvidenceBundle(hearingEvidenceDocuments)
                .build());

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, HearingBooking.builder().type(CASE_MANAGEMENT).build())))
            .hearingFurtherEvidenceDocuments(hearingEvidence)
            .applicationDocuments(applicationDocuments)
            .furtherEvidenceDocuments(furtherEvidenceHMCTS)
            .furtherEvidenceDocumentsLA(furtherEvidenceLA)
            .build();

        when(applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(
            eq(caseData), any(DocumentViewType.class)))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        when(furtherEvidenceTransformer.getFurtherEvidenceBundleView(eq(caseData), any(DocumentViewType.class)))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        when(hearingBundleTransformer.getHearingBundleView(
            eq(caseData.getHearingFurtherEvidenceDocuments()), any(DocumentViewType.class)))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        when(respondentStatementsTransformer.getRespondentStatementsBundle(eq(caseData), any(DocumentViewType.class)))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        when(documentsListRenderer.render(any())).thenReturn("<p><div class='width-50'>");

        Map<String, Object> documentViewData = documentListService.getDocumentView(caseData);

        assertThat(documentViewData).containsKeys("documentViewLA", "documentViewHMCTS", "documentViewNC");
        verify(applicationDocumentTransformer).getApplicationStatementAndDocumentBundle(caseData, LA);
        verify(furtherEvidenceTransformer).getFurtherEvidenceBundleView(caseData, LA);
        verify(hearingBundleTransformer).getHearingBundleView(caseData.getHearingFurtherEvidenceDocuments(), LA);
        verify(respondentStatementsTransformer).getRespondentStatementsBundle(caseData, LA);

        verify(applicationDocumentTransformer).getApplicationStatementAndDocumentBundle(caseData, HMCTS);
        verify(furtherEvidenceTransformer).getFurtherEvidenceBundleView(caseData, HMCTS);
        verify(hearingBundleTransformer).getHearingBundleView(caseData.getHearingFurtherEvidenceDocuments(), HMCTS);
        verify(respondentStatementsTransformer).getRespondentStatementsBundle(caseData, HMCTS);

        verify(applicationDocumentTransformer).getApplicationStatementAndDocumentBundle(caseData, NONCONFIDENTIAL);
        verify(furtherEvidenceTransformer).getFurtherEvidenceBundleView(caseData, NONCONFIDENTIAL);
        verify(hearingBundleTransformer)
            .getHearingBundleView(caseData.getHearingFurtherEvidenceDocuments(), NONCONFIDENTIAL);
        verify(respondentStatementsTransformer).getRespondentStatementsBundle(caseData, NONCONFIDENTIAL);

        verify(documentsListRenderer, atMost(3)).render(anyList());

        verifyNoMoreInteractions(applicationDocumentTransformer, furtherEvidenceTransformer,
            hearingBundleTransformer, respondentStatementsTransformer, documentsListRenderer);
    }

    @Test
    void shouldReturnEmptyMapWhenDocumentBundlesAreEmpty() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(UUID.randomUUID(),
                HearingBooking.builder().type(CASE_MANAGEMENT).build())))
            .build();

        when(applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(
            eq(caseData), any(DocumentViewType.class)))
            .thenReturn(emptyList());

        when(furtherEvidenceTransformer.getFurtherEvidenceBundleView(eq(caseData), any(DocumentViewType.class)))
            .thenReturn(emptyList());

        when(hearingBundleTransformer.getHearingBundleView(
            eq(caseData.getHearingFurtherEvidenceDocuments()), any(DocumentViewType.class)))
            .thenReturn(emptyList());

        when(respondentStatementsTransformer.getRespondentStatementsBundle(eq(caseData), any(DocumentViewType.class)))
            .thenReturn(emptyList());

        Map<String, Object> documentViewData = documentListService.getDocumentView(caseData);
        assertThat(documentViewData.get("documentViewLA")).isNull();
        assertThat(documentViewData.get("documentViewHMCTS")).isNull();
        assertThat(documentViewData.get("documentViewNC")).isNull();

        verify(applicationDocumentTransformer, atMost(3))
            .getApplicationStatementAndDocumentBundle(eq(caseData), any(DocumentViewType.class));

        verify(furtherEvidenceTransformer, atMost(3))
            .getFurtherEvidenceBundleView(eq(caseData), any(DocumentViewType.class));

        verify(hearingBundleTransformer, atMost(3))
            .getHearingBundleView(eq(caseData.getHearingFurtherEvidenceDocuments()), any(DocumentViewType.class));

        verify(respondentStatementsTransformer, atMost(3))
            .getRespondentStatementsBundle(eq(caseData), any(DocumentViewType.class));

        verifyNoInteractions(documentsListRenderer);

        verifyNoMoreInteractions(applicationDocumentTransformer, furtherEvidenceTransformer,
            hearingBundleTransformer, respondentStatementsTransformer, documentsListRenderer);
    }

    private List<Element<ApplicationDocument>> buildApplicationDocuments() {
        return wrapElements(ApplicationDocument.builder()
            .uploadedBy("Test User")
            .documentType(ApplicationDocumentType.GENOGRAM)
            .documentName("document1.docx")
            .document(testDocumentReference())
            .build());
    }

}
