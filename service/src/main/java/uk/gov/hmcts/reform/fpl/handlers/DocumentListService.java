package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.service.DocumentView;
import uk.gov.hmcts.reform.fpl.service.DocumentsListRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;


    public String search(CaseData caseData, String filter) {

        List<DocumentView> correspondenceDocuments = caseData.getCorrespondenceDocumentsLA().stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getName())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .build())
            .collect(Collectors.toList());

        List<DocumentView> applicationDocuments = caseData.getApplicationDocuments().stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getDocumentType().getLabel())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .build())
            .collect(Collectors.toList());

        List<DocumentView> all = new ArrayList<>();
        all.addAll(correspondenceDocuments);
        all.addAll(applicationDocuments);

        List<DocumentView> filtered = all.stream().filter(doc ->
            containsIgnoreCase(doc.getType(), filter)
                || containsIgnoreCase(doc.getUploadedBy(), filter)
                || containsIgnoreCase(doc.getDocument().getFilename(), filter)
                || containsIgnoreCase(doc.getUploadedAt(), filter))
            .collect(Collectors.toList());

        return documentsListRenderer.renderSearchResults(filtered);
    }


    public String getDocumentsList(CaseData caseData) {
        List<DocumentBundleView> bundles = new ArrayList<>();


        List<DocumentView> correspondenceDocuments = caseData.getCorrespondenceDocumentsLA().stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getName())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .build())
            .collect(Collectors.toList());

        List<DocumentView> applicationDocuments = caseData.getApplicationDocuments().stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .type(doc.getDocumentType().getLabel())
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .build())
            .collect(Collectors.toList());

        DocumentBundleView b1 = DocumentBundleView.builder()
            .name("Application documents")
            .documents(applicationDocuments)
            .build();

        DocumentBundleView b2 = DocumentBundleView.builder()
            .name("Correspondence")
            .documents(correspondenceDocuments)
            .build();

        if (isNotEmpty(b1.getDocuments())) {
            bundles.add(b1);
        }
        if (isNotEmpty(b2.getDocuments())) {
            bundles.add(b2);
        }

        return documentsListRenderer.render(bundles);
    }
}
