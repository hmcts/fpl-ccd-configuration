package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ScannedDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.LA;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.buildFurtherEvidenceBundle;
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
        Element<SupportingEvidenceBundle> hearingDocument1 = buildFurtherEvidenceBundle(
            "hearing evidence1", "HMCTS", false, EXPERT_REPORTS, now().minusMinutes(1));

        Element<SupportingEvidenceBundle> hearingDocument2 = buildFurtherEvidenceBundle(
            "hearing evidence2", "la@test.com", false, null, now().minusDays(1));

        Element<SupportingEvidenceBundle> hearingDocument3 = buildFurtherEvidenceBundle(
            "hearing evidence3", "HMCTS", false, null, now().minusMinutes(1));

        Element<SupportingEvidenceBundle> hearingDocument4 = buildFurtherEvidenceBundle(
            "hearing evidence3", "la@test.com", false, null, null);

        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(SCANNED_DOCUMENT_1, SCANNED_DOCUMENT_WITHOUT_DATE, SCANNED_DOCUMENT_2))
            .otherCourtAdminDocuments(wrapElements(COURT_ADMIN_DOCUMENT_1, COURT_ADMIN_DOCUMENT_2))
            .hearingFurtherEvidenceDocuments(wrapElements(HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing1")
                    .supportingEvidenceBundle(List.of(hearingDocument2, hearingDocument1)).build(),
                HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing2")
                    .supportingEvidenceBundle(List.of(hearingDocument3, hearingDocument4)).build()))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(
                    buildHearingOrderView(hearingDocument3.getValue()),
                    buildHearingOrderView(hearingDocument2.getValue()),
                    buildHearingOrderView(hearingDocument4.getValue()),
                    buildScannedDocumentView(SCANNED_DOCUMENT_2),
                    buildScannedDocumentView(SCANNED_DOCUMENT_1),
                    buildScannedDocumentView(SCANNED_DOCUMENT_WITHOUT_DATE),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_1),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_2)))
                .build());

        List<DocumentContainerView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldTransformOnlyNonConfidentialHearingEvidenceAndCourtAdminDocumentsForLA() {
        Element<SupportingEvidenceBundle> hearingDocument1 = buildFurtherEvidenceBundle(
            "hearing evidence1", "la@test.com", false, null, now().minusDays(1));

        Element<SupportingEvidenceBundle> hearingDocument2 = buildFurtherEvidenceBundle(
            "hearing evidence2", "HMCTS", true, null, now().minusDays(2));

        Element<SupportingEvidenceBundle> hearingDocument3 = buildFurtherEvidenceBundle(
            "hearing evidence3", "HMCTS", false, null, now().minusDays(3));

        Element<SupportingEvidenceBundle> hearingDocument4 = buildFurtherEvidenceBundle(
            "hearing evidence34", "la@test.com", true, null, null);

        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(SCANNED_DOCUMENT_1, SCANNED_DOCUMENT_WITHOUT_DATE, SCANNED_DOCUMENT_2))
            .otherCourtAdminDocuments(wrapElements(COURT_ADMIN_DOCUMENT_1, COURT_ADMIN_DOCUMENT_2))
            .hearingFurtherEvidenceDocuments(wrapElements(HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing1")
                    .supportingEvidenceBundle(List.of(hearingDocument1, hearingDocument2)).build(),
                HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing2")
                    .supportingEvidenceBundle(List.of(hearingDocument3, hearingDocument4)).build()))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(buildHearingOrderView(hearingDocument1.getValue()),
                    buildHearingOrderView(hearingDocument3.getValue()),
                    buildHearingOrderView(hearingDocument4.getValue()),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_1),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_2))).build());

        List<DocumentContainerView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, LA);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldTransformOnlyNonConfidentialHearingEvidenceAndCourtAdminDocumentsForNonConfidentialView() {
        Element<SupportingEvidenceBundle> laConfidentialEvidence = buildFurtherEvidenceBundle(
            "LA confidential evidence", "la@test.com", true, null, now());

        Element<SupportingEvidenceBundle> laNonConfidentialEvidence = buildFurtherEvidenceBundle(
            "LA non confidential evidence", "la@test.com", false, null, now().minusHours(1));

        Element<SupportingEvidenceBundle> hmctsNonConfidentialEvidence = buildFurtherEvidenceBundle(
            "HMCTS non confidential evidence", "HMCTS", false, null, null);

        Element<SupportingEvidenceBundle> hmctsConfidentialEvidence = buildFurtherEvidenceBundle(
            "HMCTS confidential evidence", "HMCTS", true, null, now().minusDays(1));

        CaseData caseData = CaseData.builder()
            .scannedDocuments(wrapElements(SCANNED_DOCUMENT_1, SCANNED_DOCUMENT_WITHOUT_DATE, SCANNED_DOCUMENT_2))
            .otherCourtAdminDocuments(wrapElements(COURT_ADMIN_DOCUMENT_1, COURT_ADMIN_DOCUMENT_2))
            .hearingFurtherEvidenceDocuments(wrapElements(HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing1")
                    .supportingEvidenceBundle(List.of(laNonConfidentialEvidence, hmctsConfidentialEvidence)).build(),
                HearingFurtherEvidenceBundle.builder()
                    .hearingName("hearing2")
                    .supportingEvidenceBundle(List.of(hmctsNonConfidentialEvidence, laConfidentialEvidence)).build()))
            .build();

        List<DocumentBundleView> expectedDocumentsView = List.of(
            DocumentBundleView.builder().name(DOCUMENT_BUNDLE_NAME)
                .documents(List.of(buildHearingOrderView(laNonConfidentialEvidence.getValue()),
                    buildHearingOrderView(hmctsNonConfidentialEvidence.getValue()),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_1),
                    buildCourtAdminDocumentView(COURT_ADMIN_DOCUMENT_2))).build());

        List<DocumentContainerView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, NONCONFIDENTIAL);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnDocumentViewForScannedDocumentsViewWhenOtherDocumentsAreNullOrEmpty(
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

        List<DocumentContainerView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

        assertThat(actualDocumentsView).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldReturnEmptyCollectionWhenOtherCourtAdminDocumentsViewAndScannedDocumentsAreNullOrEmpty() {
        CaseData caseData = CaseData.builder()
            .scannedDocuments(null)
            .otherCourtAdminDocuments(List.of())
            .build();

        List<DocumentContainerView> actualDocumentsView = underTest.getOtherDocumentsView(caseData, HMCTS);

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

    private DocumentView buildHearingOrderView(SupportingEvidenceBundle bundle) {
        return DocumentView.builder()
            .document(bundle.getDocument())
            .fileName(bundle.getName())
            .title(bundle.getName())
            .uploadedBy(bundle.getUploadedBy())
            .uploadedAt(isNotEmpty(bundle.getDateTimeUploaded())
                ? formatLocalDateTimeBaseUsingFormat(bundle.getDateTimeUploaded(), TIME_DATE) : null)
            .build();
    }

}
