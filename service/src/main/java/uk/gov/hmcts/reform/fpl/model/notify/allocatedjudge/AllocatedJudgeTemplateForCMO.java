package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class AllocatedJudgeTemplateForCMO extends SharedAllocatedJudgeTemplate {
    private String subjectLineWithHearingDate;
    private String respondentLastName;
    private String reference;
}
