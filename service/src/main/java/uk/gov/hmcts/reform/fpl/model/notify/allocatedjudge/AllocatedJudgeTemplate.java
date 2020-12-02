package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public final class AllocatedJudgeTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String caseName;
    private String caseUrl;
    private String familyManCaseNumber;
}
