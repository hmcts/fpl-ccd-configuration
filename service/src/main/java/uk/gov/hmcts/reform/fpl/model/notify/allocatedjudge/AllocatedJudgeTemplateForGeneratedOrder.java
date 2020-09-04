package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class AllocatedJudgeTemplateForGeneratedOrder extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;
    private String orderType;
}
