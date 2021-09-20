package uk.gov.hmcts.reform.fpl.service.document.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsBundlesTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.OtherDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleViewAggregator {

    private final ApplicationDocumentBundleTransformer applicationDocumentTransformer;
    private final FurtherEvidenceDocumentsBundlesTransformer furtherEvidenceTransformer;
    private final RespondentStatementsTransformer respondentStatementsTransformer;
    private final OtherDocumentsTransformer otherDocumentsTransformer;

    public List<DocumentContainerView> getDocumentBundleViews(
        CaseData caseData,
        DocumentViewType view) {

        DocumentContainerView applicationStatementAndDocumentsBundle =
            applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(caseData, view);

        List<DocumentContainerView> bundles = new ArrayList<>();
        if (!isNull(applicationStatementAndDocumentsBundle)) {
            bundles.add(applicationStatementAndDocumentsBundle);
        }

        List<DocumentContainerView> furtherEvidenceBundle
            = furtherEvidenceTransformer.getFurtherEvidenceDocumentsBundleView(caseData, view);

        List<DocumentContainerView> respondentStatementBundle =
            respondentStatementsTransformer.getRespondentStatementsBundle(caseData, view);

        List<DocumentContainerView> anyOtherDocumentsBundle
            = otherDocumentsTransformer.getOtherDocumentsView(caseData, view);

        bundles.addAll(furtherEvidenceBundle);
        bundles.addAll(respondentStatementBundle);
        bundles.addAll(anyOtherDocumentsBundle);

        return bundles;
    }

}
