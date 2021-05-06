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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentListService {

    private final DocumentsListRenderer documentsListRenderer;
    private final String documentViewHMCTS = "HMCTS";
    private final String documentViewLA = "LA";
    private static final String RESPONDENT_STATEMENT_DOCUMENT = "Respondent statements";

    public String getDocumentView(CaseData caseData, String view) {
        List<DocumentBundleView> bundles = new ArrayList<>();
        List<DocumentBundleView> applicationStatementAndDocumentsBundle;
        List<DocumentBundleView> furtherEvidenceBundle;
        List<DocumentBundleView> hearingEvidenceBundle;
        List<DocumentBundleView> respondentStatementBundle;

        if (view.equals(documentViewHMCTS)) {
            applicationStatementAndDocumentsBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                caseData.getHearingFurtherEvidenceDocuments(),
                true,
                true);

            furtherEvidenceBundle = getFurtherEvidenceBundle(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), true, true);

            hearingEvidenceBundle = getHearingBundle(
                caseData.getHearingFurtherEvidenceDocuments(),
                true, true);

            respondentStatementBundle = getRespondentStatementsBundle(
                caseData.getRespondentStatements(), caseData.getRespondents1(), true, true);
        } else if (view.equals(documentViewLA)) {
            applicationStatementAndDocumentsBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                caseData.getHearingFurtherEvidenceDocuments(),
                false,
                true);

            furtherEvidenceBundle = getFurtherEvidenceBundle(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), false, true);

            hearingEvidenceBundle = getHearingBundle(caseData.getHearingFurtherEvidenceDocuments(),
                false, true);

            respondentStatementBundle = getRespondentStatementsBundle(
                caseData.getRespondentStatements(), caseData.getRespondents1(), true, true);
        } else {
            applicationStatementAndDocumentsBundle = getApplicationStatementAndDocumentBundle(
                caseData.getApplicationDocuments(),
                caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(),
                caseData.getHearingFurtherEvidenceDocuments(),
                false,
                false);

            furtherEvidenceBundle = getFurtherEvidenceBundle(caseData.getFurtherEvidenceDocuments(),
                caseData.getFurtherEvidenceDocumentsLA(), false, false);

            hearingEvidenceBundle = getHearingBundle(caseData.getHearingFurtherEvidenceDocuments(),
                false, false);

            respondentStatementBundle = getRespondentStatementsBundle(
                caseData.getRespondentStatements(), caseData.getRespondents1(), true, true);
        }

        bundles = addToBundle(bundles, applicationStatementAndDocumentsBundle);
        bundles = addToBundle(bundles, furtherEvidenceBundle);
        bundles = addToBundle(bundles, hearingEvidenceBundle);
        bundles = addToBundle(bundles, respondentStatementBundle);

        return documentsListRenderer.render(bundles);
    }

    private List<DocumentBundleView> addToBundle(List<DocumentBundleView> bundle, List<DocumentBundleView> bundleToAdd) {
        if (isNotEmpty(bundleToAdd)) {
            bundle.addAll(bundleToAdd);
        }
        return bundle;
    }

    private List<DocumentBundleView> getHearingBundle(
        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA) {

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = new ArrayList<>();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = new ArrayList<>();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC = new ArrayList<>();

        unwrapElements(hearingFurtherEvidenceDocuments).forEach(bundle -> {
            furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceBundle());
            furtherEvidenceDocumentsLA.addAll(bundle.getSupportingEvidenceLA());
            furtherEvidenceDocumentsNC.addAll(bundle.getSupportingEvidenceNC());
        });

        List<DocumentBundleView> hearingFurtherEvidenceBundle = new ArrayList<>();

        if (includeConfidentialHMCTS && includeConfidentialLA) {
            hearingFurtherEvidenceBundle = addToDocumentBundles(hearingFurtherEvidenceBundle, furtherEvidenceDocuments);
        } else if (includeConfidentialLA && !includeConfidentialHMCTS) {
            hearingFurtherEvidenceBundle = addToDocumentBundles(hearingFurtherEvidenceBundle, furtherEvidenceDocumentsLA);
        } else {
            hearingFurtherEvidenceBundle = addToDocumentBundles(hearingFurtherEvidenceBundle, furtherEvidenceDocumentsNC);
        }

        return hearingFurtherEvidenceBundle;
    }

    private List<DocumentBundleView> getFurtherEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA) {

        List<DocumentBundleView> furtherEvidenceBundle = new ArrayList<>();
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {

                    List<DocumentView> furtherEvidenceDocumentView = getFurtherEvidenceDocumentView(type, furtherEvidenceDocuments, includeConfidentialHMCTS);
                    List<DocumentView> furtherEvidenceDocumentViewLA = getFurtherEvidenceDocumentView(type, furtherEvidenceDocumentsLA, includeConfidentialLA);

                    List<DocumentView> combinedDocuments = new ArrayList<>(furtherEvidenceDocumentView);
                    combinedDocuments.addAll(furtherEvidenceDocumentViewLA);

                    if (!combinedDocuments.isEmpty()) {
                        furtherEvidenceBundle.add(buildBundle(type.getLabel(), combinedDocuments));

                    }
                }
            );
        return furtherEvidenceBundle;
    }

    private List<DocumentView> getFurtherEvidenceDocumentView(FurtherEvidenceType type, List<Element<SupportingEvidenceBundle>> documents, boolean confidential) {
        if (isNotEmpty(documents)) {
            if (confidential) {
                return getConfidentialFurtherEvidenceDocumentView(
                    type, documents);
            } else {
                return getNonConfidentialFurtherEvidenceDocumentView(
                    type, documents);
            }
        }

        return emptyList();
    }

    private List<DocumentBundleView> getApplicationStatementAndDocumentBundle(
        List<Element<ApplicationDocument>> applicationDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA,
        List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA) {

        List<DocumentBundleView> applicationDocumentsBundle = new ArrayList<>();

        List<DocumentView> applicationDocumentView = getApplicationDocumentsView(applicationDocuments);
        List<DocumentView> applicantStatementDocumentView = getFurtherEvidenceDocumentView(APPLICANT_STATEMENT, furtherEvidenceDocuments, includeConfidentialHMCTS);
        List<DocumentView> applicantStatementDocumentViewLA = getFurtherEvidenceDocumentView(APPLICANT_STATEMENT, furtherEvidenceDocumentsLA, includeConfidentialLA);

        List<Element<SupportingEvidenceBundle>> hearingEvidenceDocs = new ArrayList<>();

        if (!isEmpty(hearingFurtherEvidenceDocuments)) {
            unwrapElements(hearingFurtherEvidenceDocuments).forEach(bundle -> {
                hearingEvidenceDocs.addAll(bundle.getSupportingEvidenceBundle());
            });
        }

        List<DocumentView> hearingEvidenceDocumentView = getFurtherEvidenceDocumentView(APPLICANT_STATEMENT, hearingEvidenceDocs, includeConfidentialHMCTS);

        List<DocumentView> combinedDocuments = Stream.of(applicantStatementDocumentView,
            applicantStatementDocumentViewLA,
            hearingEvidenceDocumentView,
            applicationDocumentView)
            .flatMap(Collection::stream)
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toList());

        if (!combinedDocuments.isEmpty()) {
            applicationDocumentsBundle.add(buildBundle(
                "Applicant's statements and application documents", combinedDocuments));
        }
        return applicationDocumentsBundle;
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
                    .build())
                .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
                .collect(Collectors.toList());
        }
        return applicationDocs;
    }

    private List<DocumentBundleView> getRespondentStatementsBundle(
        List<Element<RespondentStatement>> respondentStatements,
        List<Element<Respondent>> respondents,
        boolean includeConfidentialHMCTS,
        boolean includeConfidentialLA
    ) {
        List<DocumentBundleView> respondentStatementsView = new ArrayList<>();

        Map<UUID, List<RespondentStatement>> respondentStatementsById = unwrapElements(respondentStatements)
            .stream().collect(groupingBy(RespondentStatement::getRespondentId));

        int count = 1;
        for (Element<Respondent> respondentElement : nullSafeList(respondents)) {
            List<RespondentStatement> respondentDocumentBundle = respondentStatementsById.get(
                respondentElement.getId());

            if (isNotEmpty(respondentDocumentBundle)) {
                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments = new ArrayList<>();
                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA = new ArrayList<>();
                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC = new ArrayList<>();

                String bundleName = String.format("Respondent %d statements", count);

                respondentDocumentBundle.forEach(bundle -> {
                    furtherEvidenceDocuments.addAll(bundle.getSupportingEvidenceBundle());
                    furtherEvidenceDocumentsLA.addAll(bundle.getSupportingEvidenceLA());
                    furtherEvidenceDocumentsNC.addAll(bundle.getSupportingEvidenceNC());
                });

                if (includeConfidentialHMCTS && includeConfidentialLA && isNotEmpty(furtherEvidenceDocuments)) {
                    List<DocumentView> hmctsDocumentsView = getRespondentStatementsView(
                        furtherEvidenceDocuments, true);
                    respondentStatementsView.add(buildBundle(bundleName, hmctsDocumentsView));

                } else if (!includeConfidentialHMCTS && includeConfidentialLA
                    && isNotEmpty(furtherEvidenceDocumentsLA)) {
                    List<DocumentView> laDocumentsView = getRespondentStatementsView(
                        furtherEvidenceDocumentsLA, true);
                    respondentStatementsView.add(buildBundle(bundleName, laDocumentsView));

                } else if (isNotEmpty(furtherEvidenceDocumentsNC)) {
                    List<DocumentView> ncDocumentsView = getRespondentStatementsView(
                        furtherEvidenceDocumentsNC, false);
                    respondentStatementsView.add(buildBundle(bundleName, ncDocumentsView));
                }

                count++;
            }
        }

        return respondentStatementsView;
    }

    private List<DocumentView> getRespondentStatementsView(
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments,
        boolean includeConfidential) {

        return furtherEvidenceDocuments
            .stream()
            .map(Element::getValue)
            .filter(doc -> (includeConfidential || !doc.isConfidentialDocument()))
            .map(doc -> DocumentView.builder()
                .document(doc.getDocument())
                .fileName(doc.getName())
                .type(RESPONDENT_STATEMENT_DOCUMENT)
                .uploadedAt(formatLocalDateTimeBaseUsingFormat(doc.getDateTimeUploaded(), TIME_DATE))
                .uploadedBy(doc.getUploadedBy())
                .documentName(doc.getName())
                .confidential(doc.isConfidentialDocument())
                .build())
            .sorted(comparing(DocumentView::getUploadedAt, reverseOrder()))
            .collect(Collectors.toUnmodifiableList());
    }

    private DocumentBundleView buildBundle(String name, List<DocumentView> documents) {
        return DocumentBundleView.builder()
            .name(name)
            .documents(documents)
            .build();
    }

    private List<DocumentBundleView> addToDocumentBundles(List<DocumentBundleView> documentBundles, List<Element<SupportingEvidenceBundle>> supportingEvdienceBundle) {
        Arrays.stream(FurtherEvidenceType.values())
            .filter(type -> type != APPLICANT_STATEMENT)
            .forEach(
                type -> {
                    List<DocumentView> documentView = getConfidentialFurtherEvidenceDocumentView(type, supportingEvdienceBundle);

                    if (!documentView.isEmpty()) {
                        DocumentBundleView bundleView = buildBundle(type.getLabel(), documentView);
                        documentBundles.add(bundleView);
                    }
                });

        return documentBundles;
    }

    private List<DocumentView> getConfidentialFurtherEvidenceDocumentView(FurtherEvidenceType type,
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

    private List<DocumentView> getNonConfidentialFurtherEvidenceDocumentView(
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
