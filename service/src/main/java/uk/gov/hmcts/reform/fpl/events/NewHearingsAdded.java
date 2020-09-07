package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class NewHearingsAdded {
    private final CaseData caseData;
    private final List<Element<HearingBooking>> newHearings;
}
