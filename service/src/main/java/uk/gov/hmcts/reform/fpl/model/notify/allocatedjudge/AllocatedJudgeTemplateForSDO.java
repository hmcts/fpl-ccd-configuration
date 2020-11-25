package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class AllocatedJudgeTemplateForSDO extends SharedAllocatedJudgeTemplate {
    private String familyManCaseNumber;
    private String leadRespondentsName;
    private String hearingDate;
    private String callout;
}
