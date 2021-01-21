package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class CaseSummaryFinalHearingGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {

        return unwrapElements(caseData.getHearingDetails()).stream().filter(
            hearing -> hearing.getType().equals(HearingType.FINAL)
        ).findFirst().map(
            finalHearing ->
                SyntheticCaseSummary.builder()
                    .caseSummaryHasFinalHearing("Yes")
                    .caseSummaryFinalHearingDate(finalHearing.getStartDate().toLocalDate())
                    .build()
        ).orElse(SyntheticCaseSummary.builder().build());

    }
}
