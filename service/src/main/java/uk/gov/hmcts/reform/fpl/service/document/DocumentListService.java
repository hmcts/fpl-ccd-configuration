package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.DocumentView;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;

    public String getDocumentsList(CaseData caseData) {
        List<DocumentBundleView> bundles = new ArrayList<>();

        List<DocumentView> applicationStatementAndDocumentBundle = getApplicationStatementAndDocumentBundle(
            caseData.getApplicationDocuments(), caseData.getFurtherEvidenceDocumentsLA());

        DocumentBundleView applicationBundle = buildBundle("Applicant's statements and application documents", applicationStatementAndDocumentBundle);

        if (isNotEmpty(applicationBundle.getDocuments())) {
            bundles.add(applicationBundle);
        }


        List<DocumentBundleView> furtherEvidenceBundles = getFurtherEvidenceBundlesNC(caseData.getFurtherEvidenceDocuments(),
            caseData.getFurtherEvidenceDocumentsLA());

        if (isNotEmpty(furtherEvidenceBundles)) {
            furtherEvidenceBundles.stream().forEach(bundle -> bundles.add(bundle));
        }


        return documentsListRenderer.render(bundles);
    }

    public String getDocumentsListAllDocs(CaseData caseData) {
        List<DocumentBundleView> bundles = new ArrayList<>();

        List<DocumentView> applicationStatementAndDocumentBundle = getApplicationStatementAndDocumentBundle(
            caseData.getApplicationDocuments(), caseData.getFurtherEvidenceDocumentsLA());

        DocumentBundleView applicationBundle = buildBundle("Applicant's statements and application documents", applicationStatementAndDocumentBundle);

        if (isNotEmpty(applicationBundle.getDocuments())) {
            bundles.add(applicationBundle);
        }


        List<DocumentBundleView> furtherEvidenceBundles = getFurtherEvidenceBundlesIncConf(caseData.getFurtherEvidenceDocuments(),
            caseData.getFurtherEvidenceDocumentsLA());

        if (isNotEmpty(furtherEvidenceBundles)) {
            furtherEvidenceBundles.stream().forEach(bundle -> bundles.add(bundle));
        }


        return documentsListRenderer.render(bundles);
    }

    private DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }

    private List<DocumentView> getApplicationStatementAndDocumentBundle(List<Element<ApplicationDocument>> applicationDocuments,
                                                                        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA) {
        List<DocumentView> applicationDocs = applicationDocuments.stream()
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

        if (!isNull(furtherEvidenceDocumentsLA)) {
            List<DocumentView> applicantStatementDocuments = furtherEvidenceDocumentsLA.stream()
                .map(Element::getValue)
                .filter(doc -> doc.getType().equals(APPLICANT_STATEMENT))
                .map(doc -> DocumentView.builder()
                    .document(doc.getDocument())
                    .type(APPLICANT_STATEMENT.getLabel())
                    .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                    .uploadedBy(doc.getUploadedBy())
                    .documentName(doc.getName())
                    .build())
                .collect(Collectors.toList());

            return Stream.concat(applicationDocs.stream(), applicantStatementDocuments.stream())
                .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                .collect(Collectors.toList());
        }

        return applicationDocs;
    }

    private List<DocumentBundleView> getFurtherEvidenceBundlesNC(List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
                                                                 List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA) {
        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values()).forEach(
            type -> {
                List<DocumentView> documentsView = new ArrayList<>();

                if(!isNull(furtherEvidenceDocuments)) {
                    documentsView = furtherEvidenceDocuments
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
                            .build())
                        .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                        .collect(Collectors.toUnmodifiableList());
                }

                List<DocumentView> documentsViewLA = new ArrayList<>();

                if(!isNull(furtherEvidenceDocumentsLA)) {
                    documentsViewLA = furtherEvidenceDocumentsLA
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
                }

                List<DocumentView> newList = new ArrayList<>(documentsView);
                newList.addAll(documentsViewLA);

                if (!newList.isEmpty()) {
                    final DocumentBundleView bundleView = DocumentBundleView.builder()
                        .name(type.getLabel())
                        .documents(newList)
                        .build();

                    documentBundles.add(bundleView);
                }
            }
        );
        return documentBundles;
    }


    private List<DocumentBundleView> getFurtherEvidenceBundlesIncConf(List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
                                                                 List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA) {
        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values()).forEach(
            type -> {
                List<DocumentView> documentsView = new ArrayList<>();

                if(!isNull(furtherEvidenceDocuments)) {
                    documentsView = furtherEvidenceDocuments
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
                }

                List<DocumentView> documentsViewLA = new ArrayList<>();

                if(!isNull(furtherEvidenceDocumentsLA)) {
                    documentsViewLA = furtherEvidenceDocumentsLA
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
                }

                List<DocumentView> newList = new ArrayList<>(documentsView);
                newList.addAll(documentsViewLA);

                if (!newList.isEmpty()) {
                    final DocumentBundleView bundleView = DocumentBundleView.builder()
                        .name(type.getLabel())
                        .documents(newList)
                        .build();

                    documentBundles.add(bundleView);
                }
            }
        );
        return documentBundles;
    }
}
