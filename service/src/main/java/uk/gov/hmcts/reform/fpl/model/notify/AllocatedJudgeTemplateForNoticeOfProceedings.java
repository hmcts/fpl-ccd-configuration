package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AllocatedJudgeTemplateForNoticeOfProceedings extends SharedAllocatedJudgeTemplate {
    private String leadRespondentsName;
    private String familyManCaseNumber;
    private String hearingDate;

}
