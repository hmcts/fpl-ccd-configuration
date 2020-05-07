package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Judge;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder(toBuilder = true)
public class JudgeAndLegalAdvisor {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String legalAdvisorName;
    private String allocatedJudgeLabel;
    private String useAllocatedJudge;

    @JsonIgnore
    public boolean isUsingAllocatedJudge() {
        return YES.getValue().equals(useAllocatedJudge);
    }

    @JsonIgnore
    public boolean isEqualToAllocatedJudge(Judge allocatedJudge) {
        return this.getJudgeTitle() == allocatedJudge.getJudgeTitle()
            && (this.getJudgeFullName() != null && this.getJudgeFullName().equals(allocatedJudge.getJudgeFullName())
            || this.getJudgeLastName() != null && this.getJudgeLastName().equals(allocatedJudge.getJudgeLastName()));
    }
}
