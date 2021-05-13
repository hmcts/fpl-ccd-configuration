package uk.gov.hmcts.reform.fpl.service.document.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBundleTransformer {

    private final FurtherEvidenceDocumentsBundlesTransformer furtherEvidenceTransformer;

    public List<DocumentBundleView> getHearingBundleView(
        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments,
        DocumentViewType view) {

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = new ArrayList<>();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = new ArrayList<>();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC = new ArrayList<>();

        unwrapElements(hearingFurtherEvidenceDocuments).forEach(bundle -> {
            furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceBundle());
            furtherEvidenceDocumentsLA.addAll(bundle.getSupportingEvidenceLA());
            furtherEvidenceDocumentsNC.addAll(bundle.getSupportingEvidenceNC());
        });

        List<DocumentBundleView> hearingFurtherEvidenceBundle = new ArrayList<>();

        if (view == DocumentViewType.HMCTS) {
            hearingFurtherEvidenceBundle.addAll(
                furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(furtherEvidenceDocuments));
        } else if (view == DocumentViewType.LA) {
            hearingFurtherEvidenceBundle.addAll(
                furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(furtherEvidenceDocumentsLA));
        } else {
            hearingFurtherEvidenceBundle.addAll(
                furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(furtherEvidenceDocumentsNC));
        }

        return hearingFurtherEvidenceBundle;
    }
}
