package uk.gov.hmcts.reform.fpl.service.document.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceDocumentsBundlesTransformer {

    private final FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    public List<DocumentBundleView> getFurtherEvidenceDocumentsBundleView(
        CaseData caseData,
        DocumentViewType view) {

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = new ArrayList<>();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments
            = caseData.getHearingFurtherEvidenceDocuments();

        unwrapElements(hearingFurtherEvidenceDocuments).forEach(bundle -> {
            switch (view) {
                case HMCTS:
                    furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceBundle());
                    break;
                case LA:
                    furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceLA());
                    break;
                default:
                    furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceNC());
                    break;
            }
        });

        furtherEvidenceDocuments.addAll(getFurtherEvidenceDocumentsNotLinkedToHearing(caseData, view));
        return getFurtherEvidenceDocumentBundles(furtherEvidenceDocuments);
    }

    private List<Element<SupportingEvidenceBundle>> getFurtherEvidenceDocumentsNotLinkedToHearing(
        CaseData caseData,
        DocumentViewType view
    ) {
        List<Element<SupportingEvidenceBundle>> hmctsDocuments = nullSafeList(caseData.getFurtherEvidenceDocuments())
            .stream()
            .filter(doc -> (view.isIncludeConfidentialHMCTS() || !doc.getValue().isConfidentialDocument()))
            .collect(toList());

        List<Element<SupportingEvidenceBundle>> laDocuments = nullSafeList(caseData.getFurtherEvidenceDocumentsLA())
            .stream()
            .filter(doc -> (view.isIncludeConfidentialLA() || !doc.getValue().isConfidentialDocument()))
            .collect(toList());

        List<Element<SupportingEvidenceBundle>> combinedDocuments = new ArrayList<>(hmctsDocuments);
        combinedDocuments.addAll(laDocuments);

        return combinedDocuments;
    }

    private List<DocumentBundleView> getFurtherEvidenceDocumentBundles(
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
                        DocumentBundleView bundleView = buildBundle(type.getLabel(), documentView);
                        documentBundles.add(bundleView);
                    }
                });

        return documentBundles;
    }

    private DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }

}
