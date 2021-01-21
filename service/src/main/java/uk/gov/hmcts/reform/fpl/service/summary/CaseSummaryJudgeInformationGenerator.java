package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.Optional;

@Component
public class CaseSummaryJudgeInformationGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryAllocatedJudgeName(Optional.ofNullable(caseData.getAllocatedJudge())
                .map(Judge::getJudgeLastName)
                .orElse(null))
            .caseSummaryAllocatedJudgeEmail(Optional.ofNullable(caseData.getAllocatedJudge())
                .map(Judge::getJudgeEmailAddress)
                .orElse(null))
            .build();
    }

}
