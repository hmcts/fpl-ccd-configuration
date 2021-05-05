package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.DocumentView;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;

    public String getDocumentView(CaseData caseData, String view) {
        List<DocumentBundleView> bundles = new ArrayList<>();
        List<DocumentBundleView> applicationStatementAndDocumentBundle;

        if (view.equals("HMCTS")) {
            applicationStatementAndDocumentBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                true,
                true);
        } else if (view.equals("LA")) {
            applicationStatementAndDocumentBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                false,
                true);
        } else {
            applicationStatementAndDocumentBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                false,
                true);
        }

        if (isNotEmpty(applicationStatementAndDocumentBundle)) {
            bundles.addAll(applicationStatementAndDocumentBundle);
        }

        List<DocumentBundleView> furtherEvidenceBundles;
        List<DocumentBundleView> hearingEvidenceBundles;

        if (view.equals("HMCTS")) {
            furtherEvidenceBundles = getFurtherEvidenceBundles(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), true, true);

            hearingEvidenceBundles = getHearingEvidenceBundles(
                caseData.getHearingFurtherEvidenceDocuments(), true, true);
        } else if (view.equals("LA")) {
            furtherEvidenceBundles = getFurtherEvidenceBundles(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), false, true);

            hearingEvidenceBundles = getHearingEvidenceBundles(
                caseData.getHearingFurtherEvidenceDocuments(), false, true);
        } else {
            furtherEvidenceBundles = getFurtherEvidenceBundles(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), false, false);

            hearingEvidenceBundles = getHearingEvidenceBundles(
                caseData.getHearingFurtherEvidenceDocuments(), false, false);
        }

        if (isNotEmpty(furtherEvidenceBundles)) {
            bundles.addAll(furtherEvidenceBundles);
        }

        if (isNotEmpty(hearingEvidenceBundles)) {
            bundles.addAll(hearingEvidenceBundles);
        }

        return documentsListRenderer.render(bundles);
    }

    private List<DocumentBundleView> getHearingEvidenceBundles(
        List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA
    ) {

        if (isEmpty(hearingEvidenceDocuments)) {
            return List.of();
        }

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = new ArrayList<>();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = new ArrayList<>();

        unwrapElements(hearingEvidenceDocuments).forEach(bundle -> {
            furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceBundle());
            furtherEvidenceDocumentsLA.addAll(bundle.getSupportingEvidenceLA());
        });

        return getFurtherEvidenceBundles(
            furtherEvidenceDocuments,
            furtherEvidenceDocumentsLA,
            includeConfidentialHMCTS,
            includeConfidentialLA);

    }

    private DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }

    private List<DocumentBundleView> getApplicationStatementAndDocumentBundle(
        List<Element<ApplicationDocument>> applicationDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA) {

        List<DocumentBundleView> applicationDocumentBundle = new ArrayList<>();

        List<DocumentView> applicationDocs = new ArrayList<>();
        List<DocumentView> applicantStatementDocuments = new ArrayList<>();
        List<DocumentView> applicantStatementDocumentsLA = new ArrayList<>();
        List<DocumentView> combinedStatementDocuments = new ArrayList<>();

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
                    .build())
                .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                .collect(Collectors.toList());
        }

        if (isNotEmpty(furtherEvidenceDocuments)) {
            if (includeConfidentialHMCTS) {
                applicantStatementDocuments = getFurtherEvidenceDocumentView(
                    APPLICANT_STATEMENT, furtherEvidenceDocuments);
            } else {
                applicantStatementDocuments = getNonConfidentialDocumentView(
                    APPLICANT_STATEMENT, furtherEvidenceDocuments);
            }
        }
        if (isNotEmpty(furtherEvidenceDocumentsLA)) {
            if (includeConfidentialLA) {
                applicantStatementDocumentsLA = getFurtherEvidenceDocumentView(
                    APPLICANT_STATEMENT, furtherEvidenceDocumentsLA);
            } else {
                applicantStatementDocumentsLA = getNonConfidentialDocumentView(
                    APPLICANT_STATEMENT, furtherEvidenceDocumentsLA);
            }
        }

        List<DocumentView> newList = Stream.concat(
            applicantStatementDocuments.stream(),
            applicantStatementDocumentsLA.stream())
            .collect(Collectors.toList());

        List<DocumentView> sortedDocuments = Stream.concat(
            newList.stream(), applicationDocs.stream())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toList());

        if (!sortedDocuments.isEmpty()) {
            applicationDocumentBundle.add(buildBundle(
                "Applicant's statements and application documents", sortedDocuments));
        }

        return applicationDocumentBundle;
    }

    private List<DocumentBundleView> getFurtherEvidenceBundles(
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA) {

        List<DocumentBundleView> documentBundles = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {
                    List<DocumentView> hmctsDocumentsView = new ArrayList<>();
                    List<DocumentView> laDocumentsView = new ArrayList<>();

                    if (isNotEmpty(furtherEvidenceDocuments)) {
                        if (includeConfidentialHMCTS) {
                            hmctsDocumentsView = getFurtherEvidenceDocumentView(type, furtherEvidenceDocuments);
                        } else {
                            hmctsDocumentsView = getNonConfidentialDocumentView(type, furtherEvidenceDocuments);
                        }
                    }

                    if (isNotEmpty(furtherEvidenceDocumentsLA)) {
                        if (includeConfidentialLA) {
                            laDocumentsView = getFurtherEvidenceDocumentView(type, furtherEvidenceDocumentsLA);
                        } else {
                            laDocumentsView = getNonConfidentialDocumentView(type, furtherEvidenceDocumentsLA);
                        }
                    }

                    List<DocumentView> combinedView = new ArrayList<>(hmctsDocumentsView);
                    combinedView.addAll(laDocumentsView);

                    if (!combinedView.isEmpty()) {
                        final DocumentBundleView bundleView = DocumentBundleView.builder()
                            .name(type.getLabel())
                            .documents(combinedView)
                            .build();

                        documentBundles.add(bundleView);
                    }
                }
            );
        return documentBundles;
    }

    private List<DocumentView> getFurtherEvidenceDocumentView(
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
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toUnmodifiableList());
    }

    private List<DocumentView> getNonConfidentialDocumentView(
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
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toUnmodifiableList());
    }
}
