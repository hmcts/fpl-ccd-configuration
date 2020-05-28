package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AllocatedJudgeTemplateForNoticeOfProceedings extends SharedAllocatedJudgeTemplate {
    private String respondentLastName;
    private String familyManCaseNumber;
    private String hearingDate;
}
