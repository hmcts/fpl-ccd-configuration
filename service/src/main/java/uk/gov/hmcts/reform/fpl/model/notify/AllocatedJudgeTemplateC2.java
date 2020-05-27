package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AllocatedJudgeTemplateC2 extends SharedAllocatedJudgeTemplate {
    private String callout;
    private String respondentLastName;

}
