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
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.LA;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentBundleTransformerTest {

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    @InjectMocks
    private ApplicationDocumentBundleTransformer underTest;

    private final List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments
        = buildHearingFurtherEvidenceDocuments(UUID.randomUUID());

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA
        = buildFurtherEvidenceDocumentsLA();

    @Test
    void shouldGetApplicationDocumentBundleForHmctsView() {
        CaseData caseData = CaseData.builder()
            .applicationDocuments(buildApplicationDocuments())
            .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
            .build();

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getHearingFurtherEvidenceDocuments().get(0).getValue().getSupportingEvidenceBundle(), true))
            .thenReturn(getExpectedHearingDocumentViewHMCTS());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocuments(), true))
            .thenReturn(expectedDocumentViewHMCTS());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocumentsLA(), true))
            .thenReturn(expectedDocumentViewLA());

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(getExpectedApplicationDocumentsHMCTS())
            .build());

        List<DocumentBundleView> bundle = underTest.getApplicationStatementAndDocumentBundle(caseData, HMCTS);

        assertThat(bundle).isEqualTo(expectedBundle);
    }

    @Test
    void shouldGetApplicationDocumentBundleForLAView() {
        CaseData caseData = CaseData.builder()
            .applicationDocuments(buildApplicationDocuments())
            .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
            .build();

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getHearingFurtherEvidenceDocuments().get(0).getValue().getSupportingEvidenceBundle(), false))
            .thenReturn(getExpectedHearingDocumentViewLA());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocuments(), false))
            .thenReturn(expectedDocumentViewHmctsNonConfidential());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocumentsLA(), true))
            .thenReturn(expectedDocumentViewLA());

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(getExpectedApplicationDocumentsLA())
            .build());

        List<DocumentBundleView> bundle = underTest.getApplicationStatementAndDocumentBundle(caseData, LA);

        assertThat(bundle).isEqualTo(expectedBundle);
    }

    @Test
    void shouldGetApplicationDocumentBundleForNonConfidentialView() {
        CaseData caseData = CaseData.builder()
            .applicationDocuments(buildApplicationDocuments())
            .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
            .build();

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getHearingFurtherEvidenceDocuments().get(0).getValue().getSupportingEvidenceBundle(), false))
            .thenReturn(getExpectedHearingDocumentViewNonConfidential());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocuments(), false))
            .thenReturn(expectedDocumentViewHmctsNonConfidential());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocumentsLA(), false))
            .thenReturn(expectedDocumentViewLANonConfidential());

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(getExpectedApplicationDocumentsNonConfidential())
            .build());

        List<DocumentBundleView> bundle = underTest.getApplicationStatementAndDocumentBundle(
            caseData, NONCONFIDENTIAL);

        assertThat(bundle).isEqualTo(expectedBundle);
    }

    private List<DocumentView> getExpectedApplicationDocumentsHMCTS() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedApplicationDocumentView());
        documents.addAll(getExpectedHearingDocumentViewHMCTS());
        documents.addAll(expectedDocumentViewHMCTS());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> getExpectedApplicationDocumentsLA() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedApplicationDocumentView());
        documents.addAll(getExpectedHearingDocumentViewLA());
        documents.addAll(expectedDocumentViewHmctsNonConfidential());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> getExpectedApplicationDocumentsNonConfidential() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedApplicationDocumentView());
        documents.addAll(getExpectedHearingDocumentViewNonConfidential());
        documents.addAll(expectedDocumentViewHmctsNonConfidential());
        documents.addAll(expectedDocumentViewLANonConfidential());

        return documents;
    }

    private List<DocumentView> expectedApplicationDocumentView() {
        return List.of(DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("Threshold")
                .uploadedAt("8:20pm, 15 June 2021")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 20, 0))
                .includedInSWET(null)
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName(null)
                .title("Threshold")
                .includeSWETField(false)
                .includeDocumentName(false)
                .build(),
            (DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("SWET")
                .uploadedAt("8:19pm, 15 June 2021")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 19, 0))
                .includedInSWET("This is included in SWET")
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName(null)
                .title("SWET")
                .includeSWETField(true)
                .includeDocumentName(false)
                .build()),
            (DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("Other")
                .uploadedDateTime(LocalDateTime.of(2021, 6, 15, 20, 18, 0))
                .uploadedAt("8:18pm, 15 June 2021")
                .includedInSWET(null)
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName("")
                .title("Document name")
                .includeSWETField(false)
                .includeDocumentName(true)
                .build())
        );
    }

    private List<DocumentView> getExpectedHearingDocumentViewHMCTS() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedDocumentViewHMCTS());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> getExpectedHearingDocumentViewLA() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedDocumentViewHmctsNonConfidential());
        documents.addAll(expectedDocumentViewLA());

        return documents;
    }

    private List<DocumentView> getExpectedHearingDocumentViewNonConfidential() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedDocumentViewHmctsNonConfidential());
        documents.addAll(expectedDocumentViewLANonConfidential());

        return documents;
    }

    private List<DocumentView> expectedDocumentViewHMCTS() {
        return List.of(expectedDocumentView("Admin uploaded evidence - confidential", "HMCTS", true),
            expectedDocumentView("Admin uploaded evidence - non confidential", "HMCTS", false));
    }

    private List<DocumentView> expectedDocumentViewHmctsNonConfidential() {
        return List.of(expectedDocumentView("Admin uploaded evidence - non confidential", "HMCTS", false));
    }

    private List<DocumentView> expectedDocumentViewLA() {
        return List.of(
            expectedDocumentView("LA uploaded evidence - confidential", "Kurt solicitor", true),
            expectedDocumentView("LA uploaded evidence - non confidential", "Kurt solicitor", false));
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

    private List<DocumentView> expectedDocumentViewLANonConfidential() {
        return List.of(expectedDocumentView("LA uploaded evidence - non confidential", "Kurt solicitor", false));
    }

    private List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceDocuments(UUID hearingId) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = List.of(
            LA_CONFIDENTIAL_DOCUMENT, ADMIN_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT);

        return List.of(element(hearingId, HearingFurtherEvidenceBundle.builder()
            .supportingEvidenceBundle(furtherEvidenceBundle)
            .build()));
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsLA() {
        return List.of(LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<ApplicationDocument>> buildApplicationDocuments() {
        return List.of(
            buildApplicationDocument(ApplicationDocumentType.SWET, LocalDateTime.of(2021, 6, 15, 20, 19, 0)),
            buildApplicationDocument(ApplicationDocumentType.OTHER, LocalDateTime.of(2021, 6, 15, 20, 18, 0)),
            buildApplicationDocument(ApplicationDocumentType.THRESHOLD, LocalDateTime.of(2021, 6, 15, 20, 20, 0)));
    }

    private Element<ApplicationDocument> buildApplicationDocument(
        ApplicationDocumentType type,
        LocalDateTime uploadedAt) {
        return element(ApplicationDocument.builder()
            .documentType(type)
            .documentName(type == ApplicationDocumentType.OTHER ? "Document name" : null)
            .uploadedBy("kurt@swansea.gov.uk")
            .includedInSWET(type == ApplicationDocumentType.SWET ? "This is included in SWET" : null)
            .document(DocumentReference.builder().build())
            .dateTimeUploaded(uploadedAt)
            .build());
    }

}
