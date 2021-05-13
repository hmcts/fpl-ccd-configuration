package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.ScannedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class OtherDocumentsTransformerTest {

    private static final String DOCUMENT_BUNDLE_NAME = "Any other documents";
    public static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();

    private OtherDocumentsTransformer underTest = new OtherDocumentsTransformer();

    public static final CourtAdminDocument COURT_ADMIN_DOCUMENT_1 = CourtAdminDocument.builder()
        .document(DOCUMENT_REFERENCE).documentTitle("court-document1").build();

    public static final CourtAdminDocument COURT_ADMIN_DOCUMENT_2 = CourtAdminDocument.builder()
        .document(DOCUMENT_REFERENCE).documentTitle("court-document2").build();

    public static final ScannedDocument SCANNED_DOCUMENT_1 = ScannedDocument.builder()
        .fileName("scanned-document1").url(DOCUMENT_REFERENCE)
        .scannedDate(LocalDateTime.of(2021, 5, 9, 9, 10, 0)).build();

    public static final ScannedDocument SCANNED_DOCUMENT_2 = ScannedDocument.builder()
        .fileName("scanned-document2").url(DOCUMENT_REFERENCE)
        .scannedDate(LocalDateTime.of(2021, 5, 10, 10, 10, 0)).build();

    public static final ScannedDocument SCANNED_DOCUMENT_WITHOUT_DATE = ScannedDocument.builder()
        .fileName("scanned-document-null-date").url(DOCUMENT_REFERENCE).build();

    @Test
    void shouldTransformAllDocumentsForHmctsView() {
        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(SCANNED_DOCUMENT_1, SCANNED_DOCUMENT_WITHOUT_DATE, SCANNED_DOCUMENT_2))
            .otherCourtAdminDocuments(wrapElements(COURT_ADMIN_DOCUMENT_1, COURT_ADMIN_DOCUMENT_2))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(
                    buildScannedDocumentView(SCANNED_DOCUMENT_2),
                    buildScannedDocumentView(SCANNED_DOCUMENT_1),
                    buildScannedDocumentView(SCANNED_DOCUMENT_WITHOUT_DATE),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_1),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_2))).build());

        List<DocumentBundleView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentViewType.class, mode = EXCLUDE, names = {"HMCTS"})
    void shouldTransformOnlyCourtAdminDocumentsForLAAndNonConfidentialView(DocumentViewType viewType) {
        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(SCANNED_DOCUMENT_1, SCANNED_DOCUMENT_WITHOUT_DATE, SCANNED_DOCUMENT_2))
            .otherCourtAdminDocuments(wrapElements(COURT_ADMIN_DOCUMENT_1, COURT_ADMIN_DOCUMENT_2))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(
                    buildScannedDocumentView(SCANNED_DOCUMENT_2),
                    buildScannedDocumentView(SCANNED_DOCUMENT_1),
                    buildScannedDocumentView(SCANNED_DOCUMENT_WITHOUT_DATE),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_1),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_2))).build());

        List<DocumentBundleView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnScannedDocumentsViewWhenOtherCourtAdminDocumentsAreNullOrEmpty(
        List<Element<CourtAdminDocument>> courtAdminDocuments) {
        ScannedDocument scannedDocument1 = ScannedDocument.builder()
            .fileName("scanned-document1")
            .url(DOCUMENT_REFERENCE)
            .scannedDate(LocalDateTime.of(2021, 5, 9, 9, 10, 0)).build();

        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(scannedDocument1))
            .otherCourtAdminDocuments(courtAdminDocuments)
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(buildScannedDocumentView(scannedDocument1))).build());

        List<DocumentBundleView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnOtherCourtAdminDocumentsViewWhenScannedDocumentsAreNullOrEmpty(
        List<Element<ScannedDocument>> scannedDocuments) {

        CourtAdminDocument courtAdminDocument = CourtAdminDocument.builder()
            .document(DOCUMENT_REFERENCE).documentTitle("document1").build();

        CaseData caseData = CaseData.builder()
            .scannedDocuments(scannedDocuments)
            .otherCourtAdminDocuments(wrapElements(courtAdminDocument))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(buildCourtAdminDocumentView(courtAdminDocument))).build());

        List<DocumentBundleView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldReturnEmptyCollectionWhenOtherCourtAdminDocumentsViewAndScannedDocumentsAreNullOrEmpty() {
        CaseData caseData = CaseData.builder()
            .scannedDocuments(null)
            .otherCourtAdminDocuments(List.of())
            .build();

        List<DocumentBundleView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEmpty();
    }

    private DocumentView buildScannedDocumentView(ScannedDocument scannedDocument) {
        return DocumentView.builder()
            .document(scannedDocument.getUrl())
            .fileName(scannedDocument.getFileName())
            .uploadedAt(isNotEmpty(scannedDocument.getScannedDate())
                ? formatLocalDateTimeBaseUsingFormat(scannedDocument.getScannedDate(), TIME_DATE) : null)
            .title(scannedDocument.getFileName()).build();
    }

    private DocumentView buildCourtAdminDocumentView(CourtAdminDocument courtAdminDocument) {
        return DocumentView.builder()
            .document(courtAdminDocument.getDocument())
            .fileName(courtAdminDocument.getDocumentTitle())
            .title(courtAdminDocument.getDocumentTitle()).build();
    }

}
