package uk.gov.hmcts.reform.fpl.events.judicial;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.Judge;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class NewAllocatedJudgeEvent {

    private final Judge allocatedJudge;
    private final Long caseId;

}
