package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsTransformer;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FurtherEvidenceDocumentsTransformer.class})
public class FurtherEvidenceDocumentsBundleTransformerTest {

    @Autowired
    private FurtherEvidenceDocumentsTransformer underTest;

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForHMCTSView() {
        CaseData caseData = CaseData.builder()
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .build();

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Expert reports")
            .documents((List.of(buildDocumentView(ADMIN_CONFIDENTIAL_DOCUMENT.getValue()),
                buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()))))
            .build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, DocumentViewType.HMCTS);

        assertThat(bundle).isEqualTo(expectedBundle);
    }

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForLAView() {
        CaseData caseData = CaseData.builder()
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .build();

        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Expert reports")
            .documents((List.of(buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()))))
            .build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, DocumentViewType.LA);

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

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT);
    }
}
