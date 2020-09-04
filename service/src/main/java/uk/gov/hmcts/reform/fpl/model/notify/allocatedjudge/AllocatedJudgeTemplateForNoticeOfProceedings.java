package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public final class AllocatedJudgeTemplateForNoticeOfProceedings extends SharedAllocatedJudgeTemplate {
    private String respondentLastName;
    private String familyManCaseNumber;
    private String hearingDate;
}
