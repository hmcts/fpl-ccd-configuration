package uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class SharedAllocatedJudgeTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String caseUrl;
}
