package uk.gov.hmcts.reform.fpl.events.judicial;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class NewHearingJudgeEvent {

    private final HearingBooking hearing;
    private final Long caseId;

}
