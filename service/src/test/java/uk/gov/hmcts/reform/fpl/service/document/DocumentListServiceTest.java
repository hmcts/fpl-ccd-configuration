package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.aggregator.BundleViewAggregator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentListServiceTest {

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final List<DocumentContainerView> LA_BUNDLE_VIEWS = List.of(mock(DocumentBundleView.class));
    private static final List<DocumentContainerView> HMCTS_BUNDLE_VIEWS = List.of(mock(DocumentBundleView.class));
    private static final List<DocumentContainerView> NON_CONFIDENTIAL_BUNDLE_VIEWS =
        List.of(mock(DocumentContainerView.class));
    private static final String LA_RENDERED_VIEW = "LA_RENDERED_VIEW";
    private static final String HMCTS_RENDERED_VIEW = "HMCTS_RENDERED_VIEW";
    private static final String NON_CONFIDENTIAL_RENDERED_VIEW = "NON_CONFIDENTIAL_RENDERED_VIEW";

    @Mock
    private DocumentsListRenderer documentsListRenderer;

    @Mock
    private BundleViewAggregator bundleViewAggregator;

    @InjectMocks
    private DocumentListService underTest;

    @Test
    void testGetDocumentViewWhenNothingToRender() {

        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA,
            DocumentViewType.LA)).thenReturn(Collections.emptyList());
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA,
            DocumentViewType.HMCTS)).thenReturn(Collections.emptyList());
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.NONCONFIDENTIAL))
            .thenReturn(Collections.emptyList());

        Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("documentViewLA", null);
        expected.put("documentViewHMCTS", null);
        expected.put("documentViewNC", null);
        expected.put("showFurtherEvidenceTab", YesNo.NO);

        Map<String, Object> actual = underTest.getDocumentView(CASE_DATA);

        assertThat(actual).isEqualTo(expected);

        verifyNoInteractions(documentsListRenderer);
    }

    @Test
    void testGetDocumentViewWhenAllRendered() {

        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.LA))
            .thenReturn(LA_BUNDLE_VIEWS);
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.HMCTS))
            .thenReturn(HMCTS_BUNDLE_VIEWS);
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.NONCONFIDENTIAL))
            .thenReturn(NON_CONFIDENTIAL_BUNDLE_VIEWS);

        when(documentsListRenderer.render(LA_BUNDLE_VIEWS)).thenReturn(LA_RENDERED_VIEW);
        when(documentsListRenderer.render(HMCTS_BUNDLE_VIEWS)).thenReturn(HMCTS_RENDERED_VIEW);
        when(documentsListRenderer.render(NON_CONFIDENTIAL_BUNDLE_VIEWS)).thenReturn(NON_CONFIDENTIAL_RENDERED_VIEW);

        Map<String, Object> actual = underTest.getDocumentView(CASE_DATA);

        assertThat(actual).isEqualTo(Map.of(
            "documentViewLA", LA_RENDERED_VIEW,
            "documentViewHMCTS", HMCTS_RENDERED_VIEW,
            "documentViewNC", NON_CONFIDENTIAL_RENDERED_VIEW,
            "showFurtherEvidenceTab", YesNo.YES
        ));
    }

    @Test
    void testGetDocumentViewWhenSomethingRendered() {

        when(documentsListRenderer.render(HMCTS_BUNDLE_VIEWS)).thenReturn(HMCTS_RENDERED_VIEW);

        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.LA))
            .thenReturn(Collections.emptyList());
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.HMCTS))
            .thenReturn(HMCTS_BUNDLE_VIEWS);
        when(bundleViewAggregator.getDocumentBundleViews(CASE_DATA, DocumentViewType.NONCONFIDENTIAL))
            .thenReturn(Collections.emptyList());


        Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("documentViewLA", null);
        expected.put("documentViewHMCTS", HMCTS_RENDERED_VIEW);
        expected.put("documentViewNC", null);
        expected.put("showFurtherEvidenceTab", YesNo.YES);

        Map<String, Object> actual = underTest.getDocumentView(CASE_DATA);

        assertThat(actual).isEqualTo(expected);
    }

}
