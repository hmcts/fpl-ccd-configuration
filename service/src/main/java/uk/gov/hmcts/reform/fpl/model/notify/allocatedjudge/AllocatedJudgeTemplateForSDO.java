
package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class AllocatedJudgeTemplateForSDO extends SharedAllocatedJudgeTemplate {
    private String familyManCaseNumber;
    private String leadRespondentsName;
    private String hearingDate;
    private String callout;
}
