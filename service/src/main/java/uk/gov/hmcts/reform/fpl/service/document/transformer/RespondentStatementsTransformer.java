package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class RespondentStatementsTransformer {

    private static final String RESPONDENT_STATEMENT_DOCUMENT = "Respondent statements";

    public List<DocumentBundleView> getRespondentStatementsBundle(
        CaseData caseData,
        DocumentViewType view) {

        List<Element<RespondentStatement>> respondentStatements = caseData.getRespondentStatements();
        List<Element<Respondent>> respondents = caseData.getRespondents1();

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

                List<DocumentView> documentViewList;

                if (view == DocumentViewType.HMCTS) {
                    documentViewList = getRespondentStatementsView(furtherEvidenceDocuments, true);
                } else if (view == DocumentViewType.LA) {
                    documentViewList = getRespondentStatementsView(furtherEvidenceDocumentsLA, true);
                } else {
                    documentViewList = getRespondentStatementsView(furtherEvidenceDocumentsNC, false);
                }

                if (isNotEmpty(documentViewList)) {
                    respondentStatementsView.add(buildBundle(bundleName, documentViewList));
                }
            }
            count++;
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
                .title(doc.getName())
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
}
