package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AllocatedJudgeTemplateForC2 extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;

}
