package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@SuperBuilder
public class SharedAllocatedJudgeTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String caseUrl;
}
