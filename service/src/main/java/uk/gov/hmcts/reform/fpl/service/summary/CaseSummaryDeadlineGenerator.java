package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.Optional;

@Component
public class CaseSummaryDeadlineGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryDateOfIssue(Optional.ofNullable(caseData.getDateSubmitted()).orElse(null))
            .deadline26week(Optional.ofNullable(caseData.getDateSubmitted())
                .map(date -> date.plusWeeks(26))
                .orElse(null))
            .build();
    }
}
