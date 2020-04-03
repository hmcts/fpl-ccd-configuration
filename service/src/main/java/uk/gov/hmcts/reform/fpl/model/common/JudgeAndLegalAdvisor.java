package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder
public class JudgeAndLegalAdvisor {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String legalAdvisorName;
    private final String allocatedJudgeLabel;
    private final String useAllocatedJudge;

    @JsonIgnore
    public boolean useAllocatedJudge() {
        return YES.getValue().equals(useAllocatedJudge);
    }
}
