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

    public static JudgeAndLegalAdvisor from(final Judge allocatedJudge) {
        JudgeAndLegalAdvisorBuilder judgeAndLegalAdvisorBuilder = JudgeAndLegalAdvisor.builder();
        if (allocatedJudge != null) {
            judgeAndLegalAdvisorBuilder
                .judgeTitle(allocatedJudge.getJudgeTitle())
                .otherTitle(allocatedJudge.getOtherTitle())
                .judgeLastName(allocatedJudge.getJudgeLastName())
                .judgeFullName(allocatedJudge.getJudgeFullName());
        }
        return judgeAndLegalAdvisorBuilder.build();
    }

    public JudgeAndLegalAdvisor resetJudgeProperties() {
        return JudgeAndLegalAdvisor.builder()
            .useAllocatedJudge("Yes")
            .legalAdvisorName(legalAdvisorName)
            .build();
    }
}
