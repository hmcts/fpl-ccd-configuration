package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class AllocatedJudgeTemplateForC2 extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;
}
