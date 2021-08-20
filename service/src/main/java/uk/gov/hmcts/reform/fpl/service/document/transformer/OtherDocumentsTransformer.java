package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ScannedDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class OtherDocumentsTransformer {

    private static final String DOCUMENT_BUNDLE_NAME = "Any other documents";

    public List<DocumentBundleView> getOtherDocumentsView(
        CaseData caseData,
        DocumentViewType viewType) {

        List<DocumentBundleView> documentBundleViews = new ArrayList<>();

        List<DocumentView> documentViewList = getHearingFurtherEvidenceView(caseData, viewType);

        if (viewType == DocumentViewType.HMCTS) {
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
            .sorted(comparing(ScannedDocument::getScannedDate, nullsLast(reverseOrder())))
            .map(doc -> DocumentView.builder()
                .document(doc.getUrl())
                .fileName(doc.getFileName())
                .uploadedAt(isNotEmpty(doc.getScannedDate())
                    ? formatLocalDateTimeBaseUsingFormat(doc.getScannedDate(), TIME_DATE) : null)
                .title(doc.getFileName()).build())
            .collect(toList());
    }

    private List<DocumentView> getHearingFurtherEvidenceView(
        CaseData caseData,
        DocumentViewType view) {

        return unwrapElements(caseData.getHearingFurtherEvidenceDocuments())
            .stream()
            .flatMap(bundle -> unwrapElements(getSupportingEvidenceBundles(bundle, view)).stream())
            .filter(doc -> isNull(doc.getType()))
            .sorted(comparing(SupportingEvidenceBundle::getDateTimeUploaded, nullsLast(reverseOrder())))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .translatedDocument(doc.getTranslatedDocument())
                .sentForTranslation(doc.sentForTranslation())
                .fileName(doc.getName())
                .uploadedBy(doc.getUploadedBy())
                .uploadedAt(isNotEmpty(doc.getDateTimeUploaded())
                    ? formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE) : null)
                .title(doc.getName())
                .build())
            .collect(toList());
    }

    private List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundles(
        HearingFurtherEvidenceBundle bundle,
        DocumentViewType view) {

        switch (view) {
            case HMCTS:
                return bundle.getSupportingEvidenceBundle();
            case LA:
                return bundle.getSupportingEvidenceLA();
            default:
                return bundle.getSupportingEvidenceNC();
        }
    }

    private List<DocumentView> getOtherCourtAdminDocumentsView(
        List<Element<CourtAdminDocument>> otherCourtDocuments) {

        return defaultIfNull(otherCourtDocuments, new ArrayList<Element<CourtAdminDocument>>())
            .stream()
            .map(Element::getValue)
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .title(doc.getDocumentTitle())
                .fileName(doc.getDocumentTitle()).build())
            .collect(toList());
    }

    private DocumentBundleView buildBundle(List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(DOCUMENT_BUNDLE_NAME)
            .documents(documents)
            .build();
    }
}
