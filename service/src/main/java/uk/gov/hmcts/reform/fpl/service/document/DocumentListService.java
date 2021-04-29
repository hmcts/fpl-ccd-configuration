package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DocumentView;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.DocumentBundleView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            .collect(Collectors.toList());

        applicationDocuments.sort(comparing(e -> e.getUploadedAt(), reverseOrder()));

        DocumentBundleView b1 = DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(applicationDocuments)
            .build();

        if (isNotEmpty(b1.getDocuments())) {
            bundles.add(b1);
        }

        String render = documentsListRenderer.render(bundles);

        return render;
    }
}
