package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AllocatedJudgeTemplate extends SharedNotifyTemplate {
    private String judgeTitle;
    private String judgeName;
}
