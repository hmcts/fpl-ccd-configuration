package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.UserService;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
public class CaseSummaryCaseFlagGenerator implements CaseSummaryFieldsGenerator {

    private final UserService userService;

    public CaseSummaryCaseFlagGenerator(UserService userService) {
        this.userService = userService;
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
        return caseData.getCaseFlagValueUpdated() != null
            && caseData.getCaseFlagValueUpdated().equals(YES)
            ? userService.getUserEmail()
            : caseData.getSyntheticCaseSummary().getCaseSummaryFlagAddedByEmail();
    }

    private String generateUploadedFullName(CaseData caseData) {
        return caseData.getCaseFlagValueUpdated() != null
            && caseData.getCaseFlagValueUpdated().equals(YES)
            ? userService.getUserName()
            : caseData.getSyntheticCaseSummary().getCaseSummaryFlagAddedByFullName();
    }
}
