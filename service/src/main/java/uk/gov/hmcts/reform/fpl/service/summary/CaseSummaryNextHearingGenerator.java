package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

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
            hearing -> hearing.getEndDate().compareTo(time.now()) >= 0
        ).min(comparing(HearingBooking::getEndDate)).map(
            nextHearing -> SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("Yes")
                .caseSummaryNextHearingType(nextHearing.getType().getLabel())
                .caseSummaryNextHearingDate(nextHearing.getStartDate().toLocalDate())
                .caseSummaryNextHearingJudge(nextHearing.getHearingJudgeLabel()) // todo: check if true
                .caseSummaryNextHearingEmailAddress(null)// todo:find this
                .caseSummaryNextHearingCMO(getCMO(nextHearing, caseData.getDraftUploadedCMOs()))
                .build()
        ).orElse(SyntheticCaseSummary.builder().build());

    }

    private DocumentReference getCMO(HearingBooking hearingBooking, List<Element<CaseManagementOrder>> cmos) {
        return ElementUtils.findElement(hearingBooking.getCaseManagementOrderId(), cmos)
            .map(cmo -> cmo.getValue().getOrder())
            .orElse(null);
    }
}
