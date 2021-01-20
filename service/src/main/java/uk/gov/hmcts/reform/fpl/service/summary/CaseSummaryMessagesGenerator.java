package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.Map;

@Component
public class CaseSummaryMessagesGenerator implements CaseSummaryFieldsGenerator {
    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryHasUnresolvedMessages(
                caseData.getJudicialMessages().size() > 0 ? "Yes" : "No"
            ).build();
    }
}
