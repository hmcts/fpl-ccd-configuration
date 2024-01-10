package uk.gov.hmcts.reform.fpl.service.document.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsBundlesTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.OtherDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.CTSC;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.LA;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.APPLICANTS_OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.APPLICANTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CONTACT_NOTES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DOCUMENTS_FILED_ON_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DRUG_AND_ALCOHOL_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.FAMILY_AND_VIABILITY_ASSESSMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.GUARDIAN_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.JUDGEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.LETTER_OF_INSTRUCTION;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.MEETING_NOTES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.NOTICE_OF_ACTING_OR_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PARENT_ASSESSMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POLICE_DISCLOSURE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS_RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.RESPONDENTS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.RESPONDENTS_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.SKELETON_ARGUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.TRANSCRIPTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleViewAggregator {

    private final ApplicationDocumentBundleTransformer applicationDocumentTransformer;
    private final FurtherEvidenceDocumentsBundlesTransformer furtherEvidenceTransformer;
    private final RespondentStatementsTransformer respondentStatementsTransformer;
    private final OtherDocumentsTransformer otherDocumentsTransformer;
    private final ManageDocumentService manageDocumentService;

    public List<DocumentContainerView> getDocumentBundleViews(
        CaseData caseData,
        DocumentViewType view) {

        DocumentContainerView applicationStatementAndDocumentsBundle =
            applicationDocumentTransformer.getApplicationStatementAndDocumentBundle(caseData, view);

        List<DocumentContainerView> bundles = new ArrayList<>();
        if (!isNull(applicationStatementAndDocumentsBundle)) {
            bundles.add(applicationStatementAndDocumentsBundle);
        }

        List<DocumentContainerView> furtherEvidenceBundle
            = furtherEvidenceTransformer.getFurtherEvidenceDocumentsBundleView(caseData, view);

        List<DocumentContainerView> respondentStatementBundle =
            respondentStatementsTransformer.getRespondentStatementsBundle(caseData, view);

        List<DocumentContainerView> anyOtherDocumentsBundle
            = otherDocumentsTransformer.getOtherDocumentsView(caseData, view);

        bundles.addAll(furtherEvidenceBundle);
        bundles.addAll(respondentStatementBundle);
        bundles.addAll(anyOtherDocumentsBundle);

        return bundles;
    }

    private DocumentView toDocumentView(WithDocument md, ConfidentialLevel level) {
        String filename = md.getDocument().getFilename();
        return DocumentView.builder()
            .title(filename)
            .fileName(filename)
            .confidential(true)
            .confidentialToHmcts(level == CTSC)
            .uploaderType(md.getUploaderType() == null ? "unknown" : md.getUploaderType().name())
            .build();
    }

    public List<DocumentView> getConfidentialDocumentBundleViews(CaseData caseData, DocumentViewType view) {
        DocumentType[] documentTypes = new DocumentType[] {CASE_SUMMARY, POSITION_STATEMENTS, POSITION_STATEMENTS_CHILD,
            POSITION_STATEMENTS_RESPONDENT, THRESHOLD, SKELETON_ARGUMENTS, JUDGEMENTS, TRANSCRIPTS,
            DOCUMENTS_FILED_ON_ISSUE, APPLICANTS_WITNESS_STATEMENTS, CARE_PLAN, PARENT_ASSESSMENTS,
            FAMILY_AND_VIABILITY_ASSESSMENTS, APPLICANTS_OTHER_DOCUMENTS, MEETING_NOTES, CONTACT_NOTES,
            C1_APPLICATION_DOCUMENTS, C2_APPLICATION_DOCUMENTS, RESPONDENTS_STATEMENTS, RESPONDENTS_WITNESS_STATEMENTS,
            GUARDIAN_EVIDENCE, EXPERT_REPORTS, DRUG_AND_ALCOHOL_REPORTS, LETTER_OF_INSTRUCTION, POLICE_DISCLOSURE,
            MEDICAL_RECORDS, COURT_CORRESPONDENCE, NOTICE_OF_ACTING_OR_ISSUE
        };

        List<DocumentView> ret = new ArrayList<>();
        Arrays.stream(documentTypes).forEach(dt -> {
            manageDocumentService.retrieveDocuments(caseData, dt, LA).forEach(element -> {
                if (element.getValue() instanceof WithDocument) {
                    WithDocument md = (WithDocument) element.getValue();
                    ret.add(toDocumentView(md, LA));
                }
            });
            if (view == HMCTS) {
                manageDocumentService.retrieveDocuments(caseData, dt, CTSC).forEach(element -> {
                    if (element.getValue() instanceof WithDocument) {
                        WithDocument md = (WithDocument) element.getValue();
                        ret.add(toDocumentView(md, CTSC));
                    }
                });
            }
        });
        return ret;
    }

}
