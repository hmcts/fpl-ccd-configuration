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

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

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
                    List<DocumentView> furtherEvidenceDocumentView = getFurtherEvidenceDocumentsView(
                        type, furtherEvidenceDocuments, view.isIncludeConfidentialHMCTS());

                    List<DocumentView> furtherEvidenceDocumentViewLA = getFurtherEvidenceDocumentsView(
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

    public DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }

    public List<DocumentView> getFurtherEvidenceDocumentsView(
        FurtherEvidenceType type,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        boolean includeConfidential) {

        return nullSafeList(furtherEvidenceDocuments)
            .stream()
            .map(Element::getValue)
            .filter(doc -> (type == doc.getType()) && (includeConfidential || !doc.isConfidentialDocument()))
            .sorted(comparing(SupportingEvidenceBundle::getDateTimeUploaded, nullsLast(reverseOrder())))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getType().getLabel())
                .fileName(doc.getName())
                .uploadedAt(isNotEmpty(doc.getDateTimeUploaded())
                    ? formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE) : null)
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .confidential(doc.isConfidentialDocument())
                .title(type == APPLICANT_STATEMENT ? doc.getType().getLabel() : doc.getName())
                .includeDocumentName(asList(APPLICANT_STATEMENT, OTHER).contains(doc.getType()))
                .build())
            .collect(Collectors.toUnmodifiableList());
    }
}
