package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

class FurtherEvidenceDocumentsTransformerTest {

    private FurtherEvidenceDocumentsTransformer underTest = new FurtherEvidenceDocumentsTransformer();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA
        = buildFurtherEvidenceDocumentsLA();

    @Nested
    class GetFurtherEvidenceBundleView {

        @Test
        void shouldGetFurtherEvidenceDocumentBundleForHmctsView() {
            CaseData caseData = CaseData.builder()
                .furtherEvidenceDocuments(furtherEvidenceDocuments)
                .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
                .build();

            DocumentBundleView expertBundle = DocumentBundleView.builder()
                .name("Expert reports")
                .documents((List.of(buildDocumentView(ADMIN_CONFIDENTIAL_DOCUMENT.getValue()),
                    buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue())
                ))).build();

            DocumentBundleView guardianBundle = DocumentBundleView.builder()
                .name("Child's guardian reports")
                .documents((List.of(buildDocumentView(LA_CONFIDENTIAL_DOCUMENT.getValue()),
                    buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())
                ))).build();

            List<DocumentBundleView> expectedBundle = List.of(guardianBundle, expertBundle);

            List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, DocumentViewType.HMCTS);

            assertThat(bundle).isEqualTo(expectedBundle);
        }

        @Test
        void shouldGetFurtherEvidenceDocumentBundleForLAView() {
            CaseData caseData = CaseData.builder()
                .furtherEvidenceDocuments(furtherEvidenceDocuments)
                .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
                .build();

            DocumentBundleView expertBundle = DocumentBundleView.builder()
                .name("Expert reports")
                .documents((List.of(buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()))))
                .build();

            DocumentBundleView guardianBundle = DocumentBundleView.builder()
                .name("Child's guardian reports")
                .documents((List.of(buildDocumentView(LA_CONFIDENTIAL_DOCUMENT.getValue()),
                    buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())
                ))).build();

            List<DocumentBundleView> expectedBundle = List.of(guardianBundle, expertBundle);

            List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, DocumentViewType.LA);

            assertThat(bundle).isEqualTo(expectedBundle);
        }

        @Test
        void shouldGetFurtherEvidenceDocumentBundleForNonConfidentialView() {
            CaseData caseData = CaseData.builder()
                .furtherEvidenceDocuments(furtherEvidenceDocuments)
                .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
                .build();

            DocumentBundleView expertBundle = DocumentBundleView.builder()
                .name("Expert reports")
                .documents((List.of(buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()))))
                .build();

            DocumentBundleView guardianBundle = DocumentBundleView.builder()
                .name("Child's guardian reports")
                .documents((List.of(buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())
                ))).build();

            List<DocumentBundleView> expectedBundle = List.of(guardianBundle, expertBundle);

            List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, NONCONFIDENTIAL);

            assertThat(bundle).isEqualTo(expectedBundle);
        }

        private DocumentView buildDocumentView(SupportingEvidenceBundle document) {
            return DocumentView.builder()
                .document(document.getDocument())
                .fileName(document.getName())
                .type(document.getType().getLabel())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(document.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(document.getUploadedBy())
                .documentName(document.getName())
                .confidential(document.isConfidentialDocument())
                .title(document.getName())
                .build();
        }

    }

    @Nested
    class BuildBundle {

        private static final String NAME = "name";

        @Test
        void build() {
            List<DocumentView> documents = List.of(mock(DocumentView.class));

            DocumentBundleView actual = underTest.buildBundle(NAME,
                documents);

            assertThat(actual).isEqualTo(DocumentBundleView.builder()
                .name(NAME)
                .documents(documents)
                .build());
        }
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsLA() {
        return List.of(
            LA_CONFIDENTIAL_DOCUMENT,
            LA_NON_CONFIDENTIAL_DOCUMENT);
    }
}
