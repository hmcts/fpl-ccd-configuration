package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

@Component
public class CaseSummaryJudgeInformationGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryAllocatedJudgeName(caseData.getAllocatedJudge().getJudgeLastName())
            .caseSummaryAllocatedJudgeEmail(caseData.getAllocatedJudge().getJudgeEmailAddress())
            .build();
    }

}
