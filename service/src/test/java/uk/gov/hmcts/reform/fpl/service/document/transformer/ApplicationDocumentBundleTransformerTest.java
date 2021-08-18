package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentFolderView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.LA;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.SOLICITOR_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentBundleTransformerTest {

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    @InjectMocks
    private ApplicationDocumentBundleTransformer underTest;

    public static final DocumentReference THRESHOLD_DOCUMENT = DocumentReference.builder()
        .filename("thereshold-file-name").build();
    public static final DocumentReference SWET_DOCUMENT = DocumentReference.builder()
        .filename("swet-file-name").build();
    public static final DocumentReference OTHER_DOCUMENT = DocumentReference.builder()
        .filename("other-file-name").build();

    private final List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments
        = buildHearingFurtherEvidenceDocuments(UUID.randomUUID());

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA
        = buildFurtherEvidenceDocumentsLA();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsSolicitor
        = buildFurtherEvidenceDocumentsSolicitor();

    @Test
    void shouldReturnNullWhenApplicationDocumentsAndApplicantStatementsAreNull() {
        CaseData caseData = CaseData.builder().build();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, HMCTS)).isNull();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, LA)).isNull();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, NONCONFIDENTIAL)).isNull();
    }

    @Test
    void shouldReturnNullWhenApplicationDocumentsAndApplicantStatementsAreEmpty() {
        CaseData caseData = CaseData.builder()
            .applicationDocuments(List.of())
            .hearingFurtherEvidenceDocuments(List.of())
            .furtherEvidenceDocuments(List.of())
            .furtherEvidenceDocumentsLA(List.of())
            .furtherEvidenceDocumentsSolicitor(List.of())
            .build();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, HMCTS)).isNull();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, LA)).isNull();
        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, NONCONFIDENTIAL)).isNull();
    }

    @Test
    void shouldGetApplicationDocumentBundleForHmctsView() {
        CaseData caseData = caseData();

        given(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            List.of(ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                ADMIN_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                SOLICITOR_NON_CONFIDENTIAL_DOCUMENT,
                LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                LA_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT), true))
            .willReturn(getExpectedHApplicantStatementsForHmctsView());

        DocumentFolderView expectedBundle = DocumentFolderView.builder()
            .name("Applicant's statements and application documents")
            .documentBundleViews(getExpectedApplicationDocumentsHMCTS())
            .build();

        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, HMCTS))
            .isEqualTo(expectedBundle);
    }

    @Test
    void shouldGetApplicationDocumentBundleForLAView() {
        CaseData caseData = caseData();

        given(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            List.of(ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                SOLICITOR_NON_CONFIDENTIAL_DOCUMENT,
                LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                LA_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT), true))
            .willReturn(getExpectedHApplicantStatementsForLAView());

        DocumentFolderView expectedBundle = DocumentFolderView.builder()
            .name("Applicant's statements and application documents")
            .documentBundleViews(getExpectedApplicationDocumentsLA())
            .build();

        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, LA))
            .isEqualTo(expectedBundle);
    }

    @Test
    void shouldGetApplicationDocumentBundleForNonConfidentialView() {
        CaseData caseData = caseData();

        given(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            List.of(ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT,
                SOLICITOR_NON_CONFIDENTIAL_DOCUMENT,
                LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT), true))
            .willReturn(getExpectedHApplicantStatementsForLAView());

        DocumentFolderView expectedBundle = DocumentFolderView.builder()
            .name("Applicant's statements and application documents")
            .documentBundleViews(getExpectedApplicationDocumentsLA())
            .build();

        assertThat(underTest.getApplicationStatementAndDocumentBundle(caseData, NONCONFIDENTIAL))
            .isEqualTo(expectedBundle);
    }

    private List<DocumentBundleView> getExpectedApplicationDocumentsHMCTS() {
        List<DocumentBundleView> documentBundleViews = new ArrayList<>(expectedApplicationDocumentView());

        List<DocumentView> applicationStatements = List.of(
            expectedDocumentView("Application statement document1", "HMCTS", false),
            expectedDocumentView("Application statement document3", "HMCTS", true),
            expectedDocumentView("Application statement document2", "Kurt LA", false),
            expectedDocumentView("Application statement document4", "Kurt LA", true));

        documentBundleViews.add(DocumentBundleView.builder().name(APPLICANT_STATEMENT.getLabel())
            .documents(applicationStatements).build());

        return documentBundleViews;
    }

    private List<DocumentBundleView> getExpectedApplicationDocumentsLA() {
        List<DocumentBundleView> documentBundleViews = new ArrayList<>(expectedApplicationDocumentView());

        List<DocumentView> applicationStatements = List.of(
            expectedDocumentView("Application statement document1", "HMCTS", false),
            expectedDocumentView("Application statement document2", "Kurt LA", false),
            expectedDocumentView("Application statement document4", "Kurt LA", true));

        documentBundleViews.add(DocumentBundleView.builder().name(APPLICANT_STATEMENT.getLabel())
            .documents(applicationStatements).build());

        return documentBundleViews;
    }

    private List<DocumentBundleView> expectedApplicationDocumentView() {
        return List.of(DocumentBundleView.builder().name("Threshold").documents(List.of(DocumentView.builder()
                .document(THRESHOLD_DOCUMENT)
                .type("Threshold")
                .uploadedAt("8:20pm, 15 June 2021")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 20, 0))
                .includedInSWET(null)
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName(null)
                .title(THRESHOLD_DOCUMENT.getFilename())
                .includeSWETField(false)
                .includeDocumentName(false)
                .build())).build(),
            DocumentBundleView.builder().name("SWET").documents(List.of(DocumentView.builder()
                .document(SWET_DOCUMENT)
                .type("SWET")
                .uploadedAt("8:19pm, 15 June 2021")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 19, 0))
                .includedInSWET("This is included in SWET")
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName(null)
                .title(SWET_DOCUMENT.getFilename())
                .includeSWETField(true)
                .includeDocumentName(false)
                .build())).build(),
            DocumentBundleView.builder().name("Other").documents(List.of(DocumentView.builder()
                .document(OTHER_DOCUMENT)
                .type("Other")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 18, 0))
                .uploadedAt("8:18pm, 15 June 2021")
                .includedInSWET(null)
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName("")
                .title("Document name")
                .includeSWETField(false)
                .includeDocumentName(true)
                .build())).build()
        );
    }

    private List<DocumentView> getExpectedHApplicantStatementsForHmctsView() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedDocumentViewHMCTS());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> getExpectedHApplicantStatementsForLAView() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedDocumentViewHmctsNonConfidential());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> expectedDocumentViewHMCTS() {
        return List.of(
            expectedDocumentView("Application statement document1", "HMCTS", false),
            expectedDocumentView("Application statement document3", "HMCTS", true));
    }

    private List<DocumentView> expectedDocumentViewHmctsNonConfidential() {
        return List.of(expectedDocumentView("Application statement document1", "HMCTS", false));
    }

    private List<DocumentView> expectedDocumentViewLA() {
        return List.of(
            expectedDocumentView("Application statement document2", "Kurt LA", false),
            expectedDocumentView("Application statement document4", "Kurt LA", true));
    }

    private List<DocumentView> expectedDocumentViewLANonConfidential() {
        return List.of(expectedDocumentView("Application statement document2", "Kurt LA", false));
    }

    private List<DocumentView> expectedDocumentViewSolicitor() {
        return List.of(
            expectedDocumentView("Solicitor uploaded evidence", "External solicitor", false));
    }

    private DocumentView expectedDocumentView(String documentName, String uploadedBy, boolean isConfidential) {
        return DocumentView.builder()
            .document(DocumentReference.builder().build())
            .type("Applicant statement")
            .uploadedAt("8:20pm, 15th June 2021")
            .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 19, 20, 0))
            .includedInSWET(null)
            .uploadedBy(uploadedBy)
            .documentName(documentName)
            .title("Application statement")
            .includeSWETField(false)
            .confidential(isConfidential)
            .includeDocumentName(true)
            .build();
    }

    private List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceDocuments(UUID hearingId) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = List.of(
            LA_CONFIDENTIAL_DOCUMENT, ADMIN_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT);

        return List.of(element(hearingId, HearingFurtherEvidenceBundle.builder()
            .supportingEvidenceBundle(furtherEvidenceBundle)
            .build()));
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT, ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsLA() {
        return List.of(LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsSolicitor() {
        return List.of(SOLICITOR_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<ApplicationDocument>> buildApplicationDocuments() {
        return List.of(
            buildApplicationDocument(SWET, SWET_DOCUMENT, LocalDateTime.of(2021, 6, 15, 20, 19, 0)),
            buildApplicationDocument(OTHER, OTHER_DOCUMENT, LocalDateTime.of(2021, 6, 15, 20, 18, 0)),
            buildApplicationDocument(THRESHOLD, THRESHOLD_DOCUMENT, LocalDateTime.of(2021, 6, 15, 20, 20, 0)));
    }

    private Element<ApplicationDocument> buildApplicationDocument(
        ApplicationDocumentType type,
        DocumentReference document,
        LocalDateTime uploadedAt) {
        return element(ApplicationDocument.builder()
            .documentType(type)
            .documentName(type == OTHER ? "Document name" : null)
            .uploadedBy("kurt@swansea.gov.uk")
            .includedInSWET(type == SWET ? "This is included in SWET" : null)
            .document(document)
            .dateTimeUploaded(uploadedAt)
            .build());
    }

    private CaseData caseData() {
        return CaseData.builder()
            .applicationDocuments(buildApplicationDocuments())
            .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
            .furtherEvidenceDocumentsSolicitor(furtherEvidenceDocumentsSolicitor)
            .build();
    }

}
