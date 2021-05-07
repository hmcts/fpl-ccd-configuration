package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
public class FurtherEvidenceDocumentsTransformer {

    public List<DocumentBundleView> getFurtherEvidenceBundleView(CaseData caseData,
                                                              DocumentViewType view) {

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = caseData.getFurtherEvidenceDocuments();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = caseData.getFurtherEvidenceDocumentsLA();

        List<DocumentBundleView> furtherEvidenceBundle = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {
                    List<DocumentView> furtherEvidenceDocumentView = getFurtherEvidenceDocumentView(
                        type, furtherEvidenceDocuments, view.isIncludeConfidentialHMCTS());

                    List<DocumentView> furtherEvidenceDocumentViewLA = getFurtherEvidenceDocumentView(
                        type, furtherEvidenceDocumentsLA, view.isIncludeConfidentialLA());

                    List<DocumentView> combinedDocuments = new ArrayList<>(furtherEvidenceDocumentView);
                    combinedDocuments.addAll(furtherEvidenceDocumentViewLA);

                    if (!combinedDocuments.isEmpty()) {
                        furtherEvidenceBundle.add(buildBundle(type.getLabel(), combinedDocuments));

                    }
                }
            );
        return furtherEvidenceBundle;
    }

    public List<DocumentBundleView> getFurtherEvidenceDocumentBundles(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {

        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {
                    List<DocumentView> documentView =
                        getConfidentialFurtherEvidenceDocumentView(type, supportingEvidenceBundle);

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

    public List<DocumentView> getFurtherEvidenceDocumentView(
        FurtherEvidenceType type,
        List<Element<SupportingEvidenceBundle>> documents,
        boolean confidential) {

        if (isNotEmpty(documents)) {
            if (confidential) {
                return getConfidentialFurtherEvidenceDocumentView(type, documents);
            } else {
                return getNonConfidentialFurtherEvidenceDocumentView(type, documents);
            }
        }
        return emptyList();
    }

    public List<DocumentView> getConfidentialFurtherEvidenceDocumentView(
        FurtherEvidenceType type,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments) {

        return furtherEvidenceDocuments
            .stream()
            .map(Element::getValue)
            .filter(doc -> (type == doc.getType()))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getType().getLabel())
                .fileName(doc.getName())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .confidential(doc.isConfidentialDocument())
                .title(type == APPLICANT_STATEMENT ? doc.getType().getLabel() : doc.getName())
                .includeDocumentName(Arrays.asList(APPLICANT_STATEMENT, OTHER).contains(doc.getType()))
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toUnmodifiableList());
    }

    public List<DocumentView> getNonConfidentialFurtherEvidenceDocumentView(
        FurtherEvidenceType type,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments) {

        return furtherEvidenceDocuments
            .stream()
            .map(Element::getValue)
            .filter(doc -> (type == doc.getType()) && !doc.isConfidentialDocument())
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getType().getLabel())
                .fileName(doc.getName())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .confidential(doc.isConfidentialDocument())
                .title(type == APPLICANT_STATEMENT ? doc.getType().getLabel() : doc.getName())
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toUnmodifiableList());
    }
}
