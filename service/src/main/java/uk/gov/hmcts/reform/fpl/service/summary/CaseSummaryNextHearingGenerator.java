package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static java.util.Comparator.comparing;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class CaseSummaryNextHearingGenerator implements CaseSummaryFieldsGenerator {

    private final Time time;

    public CaseSummaryNextHearingGenerator(Time time) {
        this.time = time;
    }

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {

        return unwrapElements(caseData.getHearingDetails()).stream().filter(
            hearing -> hearing.getEndDate().isAfter(time.now())
        ).min(comparing(HearingBooking::getEndDate)).map(
            nextHearing -> SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("Yes")
                .caseSummaryNextHearingType(nextHearing.getType().getLabel())
                .caseSummaryNextHearingDate(nextHearing.getStartDate().toLocalDate())
                .caseSummaryNextHearingJudge(nextHearing.getHearingJudgeLabel())
                .caseSummaryNextHearingEmailAddress("where do I get that? TODO")
                .caseSummaryNextHearingCMO(nextHearing.hasCMOAssociation() ? "YES/ find it" : "NOPE")
                .build()
        ).orElse(SyntheticCaseSummary.builder().build());

    }
}
