package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.SharedAllocatedJudgeTemplate;

@Getter
@Setter
public final class AllocateHearingJudgeTemplate extends SharedAllocatedJudgeTemplate {
    private String hearingType;
    private String callout;
}
