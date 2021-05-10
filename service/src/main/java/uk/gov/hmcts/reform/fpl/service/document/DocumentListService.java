package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.HearingBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;
    private final ApplicationDocumentBundleTransformer applicationDocumentTransformer;
    private final FurtherEvidenceDocumentsTransformer furtherEvidenceTransformer;
    private final HearingBundleTransformer hearingBundleTransformer;
    private final RespondentStatementsTransformer respondentStatementsTransformer;

    public Map<String, Object> getDocumentView(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        String bundleViewLA = renderDocumentBundleViews(caseData, DocumentViewType.LA);
        String bundleViewHMCTS = renderDocumentBundleViews(caseData, DocumentViewType.HMCTS);
        String bundleViewNC = renderDocumentBundleViews(caseData, DocumentViewType.NONCONFIDENTIAL);

        if (isNotEmpty(bundleViewLA)) {
            data.put("documentViewLA", bundleViewLA);
        }
        if (isNotEmpty(bundleViewHMCTS)) {
            data.put("documentViewHMCTS", bundleViewHMCTS);
        }
        if (isNotEmpty(bundleViewNC)) {
            data.put("documentViewNC", bundleViewNC);
        }

        return data;
    }

    private String renderDocumentBundleViews(CaseData caseData, DocumentViewType view) {
        List<DocumentBundleView> bundles = getDocumentBundleViews(caseData, view);
        if (isNotEmpty(bundles)) {
            return documentsListRenderer.render(bundles);
        }

        return null;
    }

    private List<DocumentBundleView> getDocumentBundleViews(
        CaseData caseData,
        DocumentViewType view) {

        List<DocumentBundleView> bundles = new ArrayList<>();

        List<DocumentBundleView> applicationStatementAndDocumentsBundle =
            applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(caseData, view);

        List<DocumentBundleView> furtherEvidenceBundle =
            furtherEvidenceTransformer.getFurtherEvidenceBundleView(caseData, view);

        List<DocumentBundleView> hearingEvidenceBundle = hearingBundleTransformer.getHearingBundleView(
            caseData.getHearingFurtherEvidenceDocuments(), view);

        List<DocumentBundleView> respondentStatementBundle =
            respondentStatementsTransformer.getRespondentStatementsBundle(caseData, view);

        bundles.addAll(applicationStatementAndDocumentsBundle);
        bundles.addAll(furtherEvidenceBundle);
        bundles.addAll(hearingEvidenceBundle);
        bundles.addAll(respondentStatementBundle);

        return bundles;
    }
}
