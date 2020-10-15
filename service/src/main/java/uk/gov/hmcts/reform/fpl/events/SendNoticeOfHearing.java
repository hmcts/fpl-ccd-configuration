package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Getter
@RequiredArgsConstructor
public class SendNoticeOfHearing {
    private final CaseData caseData;
    private final HearingBooking selectedHearing;
}
