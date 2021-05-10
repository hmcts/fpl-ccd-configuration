package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsTransformer;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FurtherEvidenceDocumentsTransformer.class})
public class FurtherEvidenceDocumentsBundleTransformerTest {

    @Autowired
    private FurtherEvidenceDocumentsTransformer underTest;

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA
        = buildFurtherEvidenceDocumentsLA();

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForHMCTSView() {
        CaseData caseData = CaseData.builder()
            .furtherEvidenceDocuments(furtherEvidenceDocuments)
            .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
            .build();


        List<DocumentBundleView> expectedBundle = List.of(DocumentBundleView.builder()
            .name("Expert Reports")
            .documents(getExpectedExpertReportsDocuments())
            .build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceBundleView(caseData, DocumentViewType.HMCTS);
    }

    private List<DocumentView> getExpectedExpertReportsDocuments() {
        return List.of(DocumentView.builder()
            .document(DocumentReference.builder().build())
            .type("Expert report")
            .uploadedAt("8:20pm, 15th June 2021")
            .includedInSWET(null)
            .uploadedBy("Kurt solicitor")
            .documentName("LA uploaded evidence - non confidential")
            .title("Document name")
            .includeSWETField(false)
            .includeDocumentName(true)
            .confidential(false)
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsLA() {
        return List.of(
            LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);
    }
}
