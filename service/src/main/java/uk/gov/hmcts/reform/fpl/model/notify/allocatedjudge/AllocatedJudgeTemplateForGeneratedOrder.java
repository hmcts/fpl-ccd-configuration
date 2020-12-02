package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class AllocatedJudgeTemplateForGeneratedOrder extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;
    private String orderType;
}
