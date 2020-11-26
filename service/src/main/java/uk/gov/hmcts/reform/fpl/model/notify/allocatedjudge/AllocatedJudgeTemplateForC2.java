package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class AllocatedJudgeTemplateForC2 extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;
}
