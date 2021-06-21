package uk.gov.hmcts.reform.fpl.service.document.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsBundlesTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.OtherDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleViewAggregator {

    private final ApplicationDocumentBundleTransformer applicationDocumentTransformer;
    private final FurtherEvidenceDocumentsBundlesTransformer furtherEvidenceTransformer;
    private final RespondentStatementsTransformer respondentStatementsTransformer;
    private final OtherDocumentsTransformer otherDocumentsTransformer;

    public List<DocumentBundleView> getDocumentBundleViews(
        CaseData caseData,
        DocumentViewType view) {

        List<DocumentBundleView> bundles = new ArrayList<>();

        List<DocumentBundleView> applicationStatementAndDocumentsBundle =
            applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(caseData, view);

        List<DocumentBundleView> furtherEvidenceBundle
            = furtherEvidenceTransformer.getFurtherEvidenceDocumentsBundleView(caseData, view);

        List<DocumentBundleView> respondentStatementBundle =
            respondentStatementsTransformer.getRespondentStatementsBundle(caseData, view);

        List<DocumentBundleView> anyOtherDocumentsBundle
            = otherDocumentsTransformer.getOtherDocumentsView(caseData, view);

        bundles.addAll(applicationStatementAndDocumentsBundle);
        bundles.addAll(furtherEvidenceBundle);
        bundles.addAll(respondentStatementBundle);
        bundles.addAll(anyOtherDocumentsBundle);

        return bundles;
    }

}
