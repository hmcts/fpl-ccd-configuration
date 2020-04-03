package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder(toBuilder = true)
public class JudgeAndLegalAdvisor {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String legalAdvisorName;
    @Setter
    private String allocatedJudgeLabel;
    @Setter
    private String useAllocatedJudge;

    @JsonIgnore
    public boolean isUsingAllocatedJudge() {
        return YES.getValue().equals(useAllocatedJudge);
    }
}
