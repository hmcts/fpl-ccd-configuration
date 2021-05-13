package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.ScannedDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
public class OtherDocumentsTransformer {

    private static final String DOCUMENT_BUNDLE_NAME = "Any other documents";

    public List<DocumentBundleView> getOtherDocumentsView(
        CaseData caseData,
        DocumentViewType documentViewType) {

        List<DocumentBundleView> documentBundleViews = new ArrayList<>();
        List<DocumentView> documentViewList = new ArrayList<>();

        if (documentViewType == DocumentViewType.HMCTS) {
            documentViewList.addAll(getScannedDocumentsView(caseData.getScannedDocuments()));
        }

        documentViewList.addAll(getOtherCourtAdminDocumentsView(caseData.getOtherCourtAdminDocuments()));

        if (isNotEmpty(documentViewList)) {
            documentBundleViews.add(buildBundle(documentViewList));
        }

        return documentBundleViews;
    }

    private List<DocumentView> getScannedDocumentsView(List<Element<ScannedDocument>> scannedDocuments) {
        return defaultIfNull(scannedDocuments, new ArrayList<Element<ScannedDocument>>())
            .stream()
            .map(Element::getValue)
            .sorted(Comparator.comparing(ScannedDocument::getScannedDate, nullsLast(reverseOrder())))
            .map(doc -> DocumentView.builder()
                .document(doc.getUrl())
                .fileName(doc.getFileName())
                .uploadedAt(isNotEmpty(doc.getScannedDate())
                    ? formatLocalDateTimeBaseUsingFormat(doc.getScannedDate(), TIME_DATE) : null)
                .documentName(doc.getFileName()).build())
            .collect(Collectors.toList());
    }

    private List<DocumentView> getOtherCourtAdminDocumentsView(List<Element<CourtAdminDocument>> otherCourtDocuments) {
        return defaultIfNull(otherCourtDocuments, new ArrayList<Element<CourtAdminDocument>>())
            .stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .fileName(doc.getDocumentTitle()).build())
            .collect(Collectors.toList());
    }

    private DocumentBundleView buildBundle(List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(DOCUMENT_BUNDLE_NAME)
            .documents(documents)
            .build();
    }
}
