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

        data.put("documentViewLA", renderDocumentBundleViews(caseData, DocumentViewType.LA));
        data.put("documentViewHMCTS", renderDocumentBundleViews(caseData, DocumentViewType.HMCTS));
        data.put("documentViewNC", renderDocumentBundleViews(caseData, DocumentViewType.NONCONFIDENTIAL));

        return data;
    }

    private String renderDocumentBundleViews(CaseData caseData, DocumentViewType view) {
        List<DocumentBundleView> bundles = getDocumentBundleViews(caseData, view);
        return documentsListRenderer.render(bundles);
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
