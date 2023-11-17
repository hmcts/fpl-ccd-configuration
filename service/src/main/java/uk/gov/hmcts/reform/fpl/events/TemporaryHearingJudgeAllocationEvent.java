package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

/**
 * Event to notify the judge that they have been allocated as the hearing judge.
 * @deprecated (since DFPL-1862, this notification is no longer necessary)
 */
@Getter
@Deprecated(since = "DFPL-1862")
@RequiredArgsConstructor
public class TemporaryHearingJudgeAllocationEvent {
    private final CaseData caseData;
    private final HearingBooking selectedHearing;
}
