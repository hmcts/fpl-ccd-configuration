package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class CaseSummaryCaseFlagGenerator implements CaseSummaryFieldsGenerator {

    private final DocumentUploadHelper documentUploadHelper;

    public CaseSummaryCaseFlagGenerator(DocumentUploadHelper documentUploadHelper) {
        this.documentUploadHelper = documentUploadHelper;
    }

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryFlagAssessmentForm(caseData.getRedDotAssessmentForm())
            .caseSummaryCaseFlagNotes(caseData.getCaseFlagNotes())
            .caseSummaryFlagAddedByFullName(generateUploadedFullName(caseData))
            .caseSummaryFlagAddedByEmail(generateUploadedByEmail(caseData))
            .build();
    }

    private String generateUploadedByEmail(CaseData caseData) {
        return caseData.getCaseFlagValueUpdated().equals(YES)
            ? documentUploadHelper.getUploadedDocumentUserDetails()
            : caseData.getCaseSummaryFlagAddedByEmail();
    }

    private String generateUploadedFullName(CaseData caseData) {
        return caseData.getCaseFlagValueUpdated().equals(YES)
            ? documentUploadHelper.getUploadedDocumentName()
            : caseData.getCaseSummaryFlagAddedByFullName();
    }
}
