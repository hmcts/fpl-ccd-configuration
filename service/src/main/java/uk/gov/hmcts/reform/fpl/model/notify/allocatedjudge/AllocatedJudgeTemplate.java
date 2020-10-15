package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Setter
public final class AllocatedJudgeTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String caseName;
    private String caseUrl;
    private String familyManCaseNumber;
}
