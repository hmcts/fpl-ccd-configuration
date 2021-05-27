package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceDocumentsBundlesTransformerTest {

    private static final List<Element<SupportingEvidenceBundle>> SUPPORTING_EVIDENCE_BUNDLE =
        List.of(element(UUID.randomUUID(), mock(SupportingEvidenceBundle.class)));
    private static final List<DocumentView> EXPERT_REPORTS_DOCUMENT_VIEWS = List.of(mock(DocumentView.class));
    private static final List<DocumentView> GUARDIAN_REPORTS_DOCUMENT_VIEWS = List.of(mock(DocumentView.class));
    private static final List<DocumentView> OTHERS_REPORTS_DOCUMENT_VIEWS = List.of(mock(DocumentView.class));
    private static final DocumentBundleView EXPORT_REPORTS_BUNDLE_VIEW = mock(DocumentBundleView.class);
    private static final DocumentBundleView GUARDIAN_REPORTS_BUNDLE_VIEW = mock(DocumentBundleView.class);
    private static final DocumentBundleView OTHERS_REPORTS_BUNDLE_VIEW = mock(DocumentBundleView.class);

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    @InjectMocks
    private FurtherEvidenceDocumentsBundlesTransformer underTest;

    @Test
    void testGetFurtherEvidenceDocumentBundles() {

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(EXPERT_REPORTS_DOCUMENT_VIEWS);
        when(furtherEvidenceDocumentsTransformer.buildBundle(EXPERT_REPORTS.getLabel(), EXPERT_REPORTS_DOCUMENT_VIEWS))
            .thenReturn(EXPORT_REPORTS_BUNDLE_VIEW);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(GUARDIAN_REPORTS_DOCUMENT_VIEWS);
        when(furtherEvidenceDocumentsTransformer.buildBundle(GUARDIAN_REPORTS.getLabel(),
            GUARDIAN_REPORTS_DOCUMENT_VIEWS))
            .thenReturn(GUARDIAN_REPORTS_BUNDLE_VIEW);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            OTHER_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(OTHERS_REPORTS_DOCUMENT_VIEWS);
        when(furtherEvidenceDocumentsTransformer.buildBundle(OTHER_REPORTS.getLabel(), OTHERS_REPORTS_DOCUMENT_VIEWS))
            .thenReturn(OTHERS_REPORTS_BUNDLE_VIEW);


        List<DocumentBundleView> actual = underTest.getFurtherEvidenceDocumentBundles(SUPPORTING_EVIDENCE_BUNDLE);

        assertThat(actual).isEqualTo(List.of(
            GUARDIAN_REPORTS_BUNDLE_VIEW,
            EXPORT_REPORTS_BUNDLE_VIEW,
            OTHERS_REPORTS_BUNDLE_VIEW
        ));

        verify(furtherEvidenceDocumentsTransformer, never()).getFurtherEvidenceDocumentsView(
            eq(APPLICANT_STATEMENT), anyList(), anyBoolean());

    }

    @Test
    void testBundlesWithTypeApplicationStatement() {
        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(Collections.emptyList());


        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(Collections.emptyList());

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            OTHER_REPORTS, SUPPORTING_EVIDENCE_BUNDLE, true)
        ).thenReturn(Collections.emptyList());

        List<DocumentBundleView> actual = underTest.getFurtherEvidenceDocumentBundles(SUPPORTING_EVIDENCE_BUNDLE);

        assertThat(actual).isEqualTo(Collections.emptyList());

        verify(furtherEvidenceDocumentsTransformer, never()).buildBundle(anyString(), anyList());
    }


}
