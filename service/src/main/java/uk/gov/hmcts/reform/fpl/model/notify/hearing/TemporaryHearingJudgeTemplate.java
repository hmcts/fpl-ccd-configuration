package uk.gov.hmcts.reform.fpl.model.notify.hearing;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.SharedAllocatedJudgeTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class TemporaryHearingJudgeTemplate extends SharedAllocatedJudgeTemplate {
    private String hearingType;
    private String callout;
    private String hasAllocatedJudge;
    private String allocatedJudgeTitle;
    private String allocatedJudgeName;
}
