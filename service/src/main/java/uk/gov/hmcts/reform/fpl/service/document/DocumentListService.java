package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.aggregator.BundleViewAggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;
    private final BundleViewAggregator bundleViewAggregator;

    public Map<String, Object> getDocumentView(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        data.put("documentViewLA", renderDocumentBundleViews(caseData, DocumentViewType.LA));
        data.put("documentViewHMCTS", renderDocumentBundleViews(caseData, DocumentViewType.HMCTS));
        data.put("documentViewNC", renderDocumentBundleViews(caseData, DocumentViewType.NONCONFIDENTIAL));

        data.put("showFurtherEvidenceTab", YesNo.from(hasAnyDocumentRendered(data)).getValue());

        return data;
    }

    private boolean hasAnyDocumentRendered(Map<String, Object> data) {
        return data.values()
            .stream()
            .anyMatch(Objects::nonNull);
    }

    private String renderDocumentBundleViews(CaseData caseData, DocumentViewType view) {
        List<DocumentBundleView> bundles = bundleViewAggregator.getDocumentBundleViews(caseData, view);
        if (isNotEmpty(bundles)) {
            return documentsListRenderer.render(bundles);
        }

        return null;
    }

}
