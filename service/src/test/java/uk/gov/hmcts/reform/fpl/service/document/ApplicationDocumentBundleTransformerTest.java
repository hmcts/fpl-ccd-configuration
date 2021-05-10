package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsTransformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT_APPLICANT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT_APPLICANT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationDocumentBundleTransformer.class, FurtherEvidenceDocumentsTransformer.class})
public class ApplicationDocumentBundleTransformerTest {

    @MockBean
    private FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    @Autowired
    private ApplicationDocumentBundleTransformer underTest;

    private final List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments
        = buildHearingFurtherEvidenceDocuments(UUID.randomUUID());

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    @Test
    void shouldGetApplicationDocumentsBundleForHMCTSView() {
        CaseData caseData = CaseData.builder()
            .applicationDocuments(buildApplicationDocuments())
            .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .build();

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(FurtherEvidenceType.APPLICANT_STATEMENT,
            caseData.getHearingFurtherEvidenceDocuments().get(0).getValue().getSupportingEvidenceBundle(), true))
            .thenReturn(expectedHearingDocumentView());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(FurtherEvidenceType.APPLICANT_STATEMENT,
            caseData.getFurtherEvidenceDocuments(), true))
            .thenReturn(expectedFurtherEvidenceDocumentView());

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .documents(getExpectedApplicationDocuments())
            .build());

        List<DocumentBundleView> bundle = underTest.getApplicationStatementAndDocumentBundle(caseData, DocumentViewType.HMCTS);

        assertThat(bundle.get(0).getDocuments()).isEqualTo(expectedBundle.get(0).getDocuments());
        assertThat(bundle.get(0).getDocuments()).hasSize(7);
        assertThat(bundle).hasSize(1);
    }

    private List<DocumentView> getExpectedApplicationDocuments() {
        List<DocumentView> documents = new ArrayList<>();
        documents.addAll(expectedFurtherEvidenceDocumentView());
        documents.addAll(expectedHearingDocumentView());
        documents.addAll(expectedApplicationDocumentView());

        return documents;
    }

    private List<DocumentView> expectedApplicationDocumentView() {
        return List.of(DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("Threshold")
                .uploadedAt("8:20pm, 15 June 2021")
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
                .uploadedAt("8:20pm, 15 June 2021")
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
                .uploadedAt("8:20pm, 15 June 2021")
                .includedInSWET(null)
                .uploadedBy("kurt@swansea.gov.uk")
                .documentName("Document name")
                .title("Other")
                .includeSWETField(false)
                .includeDocumentName(true)
                .build())
        );
    }

    private List<DocumentView> expectedHearingDocumentView() {
        return List.of(DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("Applicant statement")
                .uploadedAt("8:20pm, 15th June 2021")
                .includedInSWET(null)
                .uploadedBy("Kurt solicitor")
                .documentName("LA uploaded evidence2")
                .title("Application statement")
                .includeSWETField(false)
                .includeDocumentName(true)
                .confidential(true)
                .build(), DocumentView.builder()
                .document(DocumentReference.builder().build())
                .type("Applicant statement")
                .uploadedAt("8:20pm, 15th June 2021")
                .includedInSWET(null)
                .uploadedBy("HMCTS")
                .documentName("Admin uploaded evidence1")
                .title("Application statement")
                .includeSWETField(false)
                .confidential(false)
                .includeDocumentName(true)
                .build());
    }

    private List<DocumentView> expectedFurtherEvidenceDocumentView() {
        return List.of(DocumentView.builder()
            .document(DocumentReference.builder().build())
            .type("Applicant statement")
            .uploadedAt("8:20pm, 15th June 2021")
            .includedInSWET(null)
            .uploadedBy("Kurt solicitor")
            .documentName("LA uploaded evidence2")
            .title("Application statement")
            .includeSWETField(false)
            .includeDocumentName(true)
            .confidential(true)
            .build(), DocumentView.builder()
            .document(DocumentReference.builder().build())
            .type("Applicant statement")
            .uploadedAt("8:20pm, 15th June 2021")
            .includedInSWET(null)
            .uploadedBy("HMCTS")
            .documentName("Admin uploaded evidence1")
            .title("Application statement")
            .includeSWETField(false)
            .confidential(false)
            .includeDocumentName(true)
            .build());
    }

    private List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceDocuments(UUID hearingId) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = List.of(
            LA_CONFIDENTIAL_DOCUMENT_APPLICANT, ADMIN_NON_CONFIDENTIAL_DOCUMENT_APPLICANT);

        return List.of(element(hearingId, HearingFurtherEvidenceBundle.builder()
            .supportingEvidenceBundle(furtherEvidenceBundle)
            .build()));
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(
            LA_CONFIDENTIAL_DOCUMENT_APPLICANT, ADMIN_NON_CONFIDENTIAL_DOCUMENT_APPLICANT);
    }

    private List<Element<ApplicationDocument>> buildApplicationDocuments() {
        return List.of(element(ApplicationDocument.builder()
                .documentType(ApplicationDocumentType.THRESHOLD)
                .document(DocumentReference.builder().build())
                .dateTimeUploaded(LocalDateTime.of(2021, 6, 15, 20, 20))
                .uploadedBy("kurt@swansea.gov.uk")
                .build()),
            element(ApplicationDocument.builder()
                .documentType(ApplicationDocumentType.SWET)
                .includedInSWET("This is included in SWET")
                .dateTimeUploaded(LocalDateTime.of(2021, 6, 15, 20, 20))
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .build()),
            element(ApplicationDocument.builder()
                .documentType(ApplicationDocumentType.OTHER)
                .documentName("Document name")
                .uploadedBy("kurt@swansea.gov.uk")
                .document(DocumentReference.builder().build())
                .dateTimeUploaded(LocalDateTime.of(2021, 6, 15, 20, 20))
                .build()));
    }

}
