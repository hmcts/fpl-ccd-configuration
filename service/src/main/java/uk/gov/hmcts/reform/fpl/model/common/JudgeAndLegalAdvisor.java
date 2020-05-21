package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Judge;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
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
    private String judgeEmailAddress;

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
                .judgeFullName(allocatedJudge.getJudgeFullName())
                .judgeEmailAddress(allocatedJudge.getJudgeEmailAddress());
        }
        return judgeAndLegalAdvisorBuilder.build();
    }

    public JudgeAndLegalAdvisor reset() {
        return JudgeAndLegalAdvisor.builder()
            .useAllocatedJudge(YES.getValue())
            .legalAdvisorName(legalAdvisorName)
            .build();
    }

    @JsonIgnore
    public String getJudgeOrMagistrateTitle() {
        if (judgeTitle == OTHER) {
            return otherTitle;
        }
        return judgeTitle.getLabel();
    }

    @JsonIgnore
    public String getJudgeName() {
        if (judgeTitle == MAGISTRATES) {
            return judgeFullName;
        }
        return judgeLastName;
    }
}
