package uk.gov.hmcts.reform.fpl.service.document.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationDocumentBundleTransformer {

    private final FurtherEvidenceDocumentsTransformer furtherEvidenceTransformer;

    public List<DocumentBundleView> getApplicationStatementAndDocumentBundle(
        CaseData caseData,
        DocumentViewType view) {

        List<Element<ApplicationDocument>> applicationDocuments = caseData.getApplicationDocuments();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = caseData.getFurtherEvidenceDocuments();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = caseData.getFurtherEvidenceDocumentsLA();

        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments
            = caseData.getHearingFurtherEvidenceDocuments();

        List<DocumentBundleView> applicationDocumentsBundle = new ArrayList<>();

        List<DocumentView> applicationDocumentView = getApplicationDocumentsView(applicationDocuments);

        List<DocumentView> applicantStatementDocumentView =
            furtherEvidenceTransformer.getFurtherEvidenceDocumentsView(
                APPLICANT_STATEMENT, furtherEvidenceDocuments, view.isIncludeConfidentialHMCTS());

        List<DocumentView> applicantStatementDocumentViewLA =
            furtherEvidenceTransformer.getFurtherEvidenceDocumentsView(
                APPLICANT_STATEMENT, furtherEvidenceDocumentsLA, view.isIncludeConfidentialLA());

        List<Element<SupportingEvidenceBundle>> hearingEvidenceDocs = new ArrayList<>();

        if (!isEmpty(hearingFurtherEvidenceDocuments)) {
            unwrapElements(hearingFurtherEvidenceDocuments).forEach(
                bundle -> hearingEvidenceDocs.addAll(bundle.getSupportingEvidenceBundle()));
        }

        List<DocumentView> hearingEvidenceDocumentView =
            furtherEvidenceTransformer.getFurtherEvidenceDocumentsView(
                APPLICANT_STATEMENT, hearingEvidenceDocs, view.isIncludeConfidentialHMCTS());

        List<DocumentView> combinedDocuments = Stream.of(applicantStatementDocumentView,
            applicantStatementDocumentViewLA,
            hearingEvidenceDocumentView,
            applicationDocumentView)
            .flatMap(Collection::stream)
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toList());

        if (!combinedDocuments.isEmpty()) {
            applicationDocumentsBundle.add(buildBundle(combinedDocuments));
        }

        return applicationDocumentsBundle;
    }

    private DocumentBundleView buildBundle(List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name("Applicant's statements and application documents")
            .documents(documents)
            .build();
    }

    private List<DocumentView> getApplicationDocumentsView(List<Element<ApplicationDocument>> applicationDocuments) {
        List<DocumentView> applicationDocs = new ArrayList<>();

        if (isNotEmpty(applicationDocuments)) {
            applicationDocs = applicationDocuments.stream()
                .map(Element::getValue)
                .map(doc -> DocumentView.builder()
                    .document(doc.getDocument())
                    .type(doc.getDocumentType().getLabel())
                    .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                    .includedInSWET(doc.getIncludedInSWET())
                    .uploadedBy(doc.getUploadedBy())
                    .documentName(doc.getDocumentName())
                    .title(doc.getDocumentType().getLabel())
                    .includeSWETField(SWET == doc.getDocumentType())
                    .includeDocumentName(Arrays.asList(APPLICANT_STATEMENT, OTHER).contains(doc.getDocumentType()))
                    .build())
                .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                .collect(Collectors.toList());
        }
        return applicationDocs;
    }
}
