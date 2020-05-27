package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharedAllocatedJudgeTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String caseUrl;
}
