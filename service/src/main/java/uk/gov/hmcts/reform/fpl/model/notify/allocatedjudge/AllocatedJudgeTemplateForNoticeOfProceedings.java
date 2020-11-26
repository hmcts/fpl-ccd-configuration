package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class AllocatedJudgeTemplateForNoticeOfProceedings extends SharedAllocatedJudgeTemplate {
    private String respondentLastName;
    private String familyManCaseNumber;
    private String hearingDate;
}
