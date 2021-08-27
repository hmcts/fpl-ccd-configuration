package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Component
public class CaseSummaryWelshFlagGenerator implements CaseSummaryFieldsGenerator {
    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        String displayFlag = YesNo.fromString(defaultIfEmpty(caseData.getLanguageRequirement(), "No")).getValue();
        return SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement(displayFlag)
            .caseSummaryLanguageRequirement(displayFlag)
            .build();
    }
}
