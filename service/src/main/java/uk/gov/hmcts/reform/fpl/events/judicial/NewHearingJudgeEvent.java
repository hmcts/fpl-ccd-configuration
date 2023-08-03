package uk.gov.hmcts.reform.fpl.events.judicial;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class NewHearingJudgeEvent {

    private final HearingBooking hearing;
    private final Long caseId;

}
