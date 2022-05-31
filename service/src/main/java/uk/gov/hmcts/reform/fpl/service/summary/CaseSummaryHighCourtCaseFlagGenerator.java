package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;

@Component
public class CaseSummaryHighCourtCaseFlagGenerator implements CaseSummaryFieldsGenerator {
    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        String displayFlag = YesNo.from(
                RCJ_HIGH_COURT_CODE.equals(caseData.getCourt() == null ? "" : caseData.getCourt().getCode())
            ).getValue();
        return SyntheticCaseSummary.builder()
            .caseSummaryHighCourtCase(displayFlag)
            .build();
    }
}
