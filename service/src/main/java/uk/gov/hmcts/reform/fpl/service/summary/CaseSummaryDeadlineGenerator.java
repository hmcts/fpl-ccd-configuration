package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

@Component
public class CaseSummaryDeadlineGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryDateOfIssue(caseData.getDateSubmitted())
            .caseSummaryApplicationDeadline(caseData.getDateSubmitted().plusWeeks(26))
            .build();
    }
}
