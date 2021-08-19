package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

@Component
public class CaseSummaryCaseFlagGenerator implements CaseSummaryFieldsGenerator {

    private final DocumentUploadHelper documentUploadHelper;

    public CaseSummaryCaseFlagGenerator(DocumentUploadHelper documentUploadHelper) {
        this.documentUploadHelper = documentUploadHelper;
    }

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryFlagAddedByFullName(generateUploadedFullName())
            .caseSummaryFlagAddedByEmail(generateUploadedByEmail())
            .caseSummaryFlagAssessmentForm(caseData.getUploadAssessment())
            .caseSummaryCaseFlagNotes(caseData.getCaseFlagNotes())
            .build();
    }

    private String generateUploadedByEmail() {
        // check if new document uploaded
        return documentUploadHelper.getUploadedDocumentUserDetails();
    }

    private String generateUploadedFullName() {
        return documentUploadHelper.getUploadedDocumentName();
    }
}
