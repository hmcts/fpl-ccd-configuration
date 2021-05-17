package uk.gov.hmcts.reform.fpl.service.document.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceDocumentsBundlesTransformer {

    private final FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    public List<DocumentBundleView> getFurtherEvidenceDocumentBundles(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {
                    List<DocumentView> documentView =
                        furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(type,
                            supportingEvidenceBundle,
                            true);

                    if (!documentView.isEmpty()) {
                        DocumentBundleView bundleView = furtherEvidenceDocumentsTransformer.buildBundle(
                            type.getLabel(), documentView);
                        documentBundles.add(bundleView);
                    }
                });

        return documentBundles;
    }


}
