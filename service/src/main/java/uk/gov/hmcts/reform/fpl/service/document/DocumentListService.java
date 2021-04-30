package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.DocumentView;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;

    public String getDocumentsList(CaseData caseData) {
        List<DocumentBundleView> bundles = new ArrayList<>();

        List<DocumentView> applicationDocuments = caseData.getApplicationDocuments().stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getDocumentType().getLabel())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .includedInSWET(doc.getIncludedInSWET())
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getDocumentName())
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toList());

        List<DocumentView> applicantStatementDocuments = caseData.getFurtherEvidenceDocumentsLA().stream()
            .map(Element::getValue)
            .filter(doc -> doc.getType().equals(FurtherEvidenceType.APPLICANT_STATEMENT))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getType().getLabel())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toList());

        List<DocumentView> combinedApplicationDocuments = Stream.concat(applicationDocuments.stream(), applicantStatementDocuments.stream())
            .collect(Collectors.toList());

        DocumentBundleView b1 = DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(combinedApplicationDocuments)
            .build();

        final List<DocumentBundleView> furtherEvidenceBundles = getFurtherEvidenceBundles(caseData);

        if (isNotEmpty(b1.getDocuments())) {
            bundles.add(b1);
        }

        if (isNotEmpty(furtherEvidenceBundles)) {
            bundles.addAll(furtherEvidenceBundles);
        }

        return documentsListRenderer.render(bundles);
    }

    private List<DocumentBundleView> getFurtherEvidenceBundles(CaseData caseData) {
        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values()).forEach(
            type -> {
                final List<DocumentView> documentsView = caseData.getFurtherEvidenceDocuments()
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
                        .build())
                    .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                    .collect(Collectors.toUnmodifiableList());

                if (!documentsView.isEmpty()) {
                    final DocumentBundleView bundleView = DocumentBundleView.builder()
                        .name(type.getLabel())
                        .documents(documentsView)
                        .build();

                    documentBundles.add(bundleView);
                }
            }
        );
        return documentBundles;
    }
}
