package uk.gov.hmcts.reform.fpl.service.document.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentFolderView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationDocumentBundleTransformer {

    private final FurtherEvidenceDocumentsTransformer furtherEvidenceTransformer;

    public DocumentContainerView getApplicationStatementAndDocumentBundle(CaseData caseData,
                                                                          DocumentViewType view) {

        List<Element<ApplicationDocument>> applicationDocuments = caseData.getApplicationDocuments();
        List<Element<SupportingEvidenceBundle>> applicantStatementDocuments = getApplicantStatements(caseData, view);

        if (isEmpty(applicationDocuments) && isEmpty(applicantStatementDocuments)) {
            return null;
        }

        List<DocumentBundleView> documentsBundleViews = new ArrayList<>(
            getApplicationDocumentsBundles(applicationDocuments));

        List<DocumentView> applicantStatementsView = furtherEvidenceTransformer.getFurtherEvidenceDocumentsView(
            APPLICANT_STATEMENT, applicantStatementDocuments, true);

        if (isNotEmpty(applicantStatementsView)) {
            documentsBundleViews.add(buildBundle(APPLICANT_STATEMENT.getLabel(), applicantStatementsView));
        }

        return DocumentFolderView.builder()
            .name("Applicant's statements and application documents")
            .documentBundleViews(documentsBundleViews).build();
    }

    private List<DocumentBundleView> getApplicationDocumentsBundles(
        List<Element<ApplicationDocument>> applicationDocuments) {

        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(ApplicationDocumentType.values()).forEach(
            type -> {
                List<ApplicationDocument> documents = unwrapElements(applicationDocuments).stream()
                    .filter(document -> type == document.getDocumentType())
                    .collect(Collectors.toList());

                List<DocumentView> documentViews = getApplicationDocumentsView(documents);

                if (!documentViews.isEmpty()) {
                    documentBundles.add(buildBundle(type.getLabel(), documentViews));
                }
            });

        return documentBundles;
    }

    private List<Element<SupportingEvidenceBundle>> getApplicantStatements(CaseData caseData,
                                                                           DocumentViewType view) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = caseData.getFurtherEvidenceDocuments();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = caseData.getFurtherEvidenceDocumentsLA();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsSolicitor
            = caseData.getFurtherEvidenceDocumentsSolicitor();

        List<Element<SupportingEvidenceBundle>> applicantStatementDocuments
            = getApplicantStatementDocumentsForView(furtherEvidenceDocuments, view.isIncludeConfidentialHMCTS());

        applicantStatementDocuments.addAll(
            getApplicantStatementDocumentsForView(furtherEvidenceDocumentsLA, view.isIncludeConfidentialLA()));

        applicantStatementDocuments.addAll(
            getApplicantStatementDocumentsForView(furtherEvidenceDocumentsSolicitor, true));

        applicantStatementDocuments.addAll(
            getApplicantStatementsFromHearingEvidence(caseData.getHearingFurtherEvidenceDocuments(), view));

        return applicantStatementDocuments.stream()
            .sorted(comparing(doc -> doc.getValue().getDateTimeUploaded(), nullsLast(reverseOrder())))
            .collect(Collectors.toList());
    }

    private List<Element<SupportingEvidenceBundle>> getApplicantStatementsFromHearingEvidence(
        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments,
        DocumentViewType view) {

        List<Element<SupportingEvidenceBundle>> hearingEvidenceDocs = new ArrayList<>();

        if (!isEmpty(hearingFurtherEvidenceDocuments)) {
            unwrapElements(hearingFurtherEvidenceDocuments).forEach(
                bundle -> hearingEvidenceDocs.addAll(bundle.getSupportingEvidenceBundle()));
        }

        return getApplicantStatementDocumentsForView(hearingEvidenceDocs, view.isIncludeConfidentialHMCTS());
    }

    private List<Element<SupportingEvidenceBundle>> getApplicantStatementDocumentsForView(
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        boolean includeConfidential) {

        return nullSafeList(furtherEvidenceDocuments).stream()
            .filter(doc -> (APPLICANT_STATEMENT == doc.getValue().getType())
                && (includeConfidential || !doc.getValue().isConfidentialDocument()))
            .collect(Collectors.toList());
    }

    private List<DocumentView> getApplicationDocumentsView(List<ApplicationDocument> applicationDocuments) {
        List<DocumentView> applicationDocs = new ArrayList<>();

        if (isNotEmpty(applicationDocuments)) {
            applicationDocs = applicationDocuments.stream()
                .map(doc -> DocumentView.builder()
                    .document(doc.getDocument())
                    .type(doc.getDocumentType().getLabel())
                    .uploadedDateTime(doc.getDateTimeUploaded())
                    .uploadedAt(isNotEmpty(doc.getDateTimeUploaded())
                        ? formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE) : null)
                    .includedInSWET(doc.getIncludedInSWET())
                    .uploadedBy(doc.getUploadedBy())
                    .documentName(OTHER == doc.getDocumentType() ? EMPTY : doc.getDocumentName())
                    .title(OTHER == doc.getDocumentType() ? doc.getDocumentName() : getFilename(doc.getDocument()))
                    .includeSWETField(SWET == doc.getDocumentType())
                    .includeDocumentName(Arrays.asList(APPLICANT_STATEMENT, OTHER).contains(doc.getDocumentType()))
                    .build())
                .sorted(comparing(DocumentView::getUploadedAt, nullsLast(reverseOrder())))
                .collect(Collectors.toList());
        }
        return applicationDocs;
    }

    private String getFilename(DocumentReference documentReference) {
        return isNull(documentReference) ? EMPTY : documentReference.getFilename();
    }

    private DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }
}
