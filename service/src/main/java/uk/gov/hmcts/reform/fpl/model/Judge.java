package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

@Data
@Builder
@AllArgsConstructor
public class Judge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String judgeEmailAddress;

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

    @JsonIgnore
    public boolean equalsJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        Judge judge = new Judge(judgeAndLegalAdvisor.getJudgeTitle(), judgeAndLegalAdvisor.getOtherTitle(),
            judgeAndLegalAdvisor.getJudgeLastName(), judgeAndLegalAdvisor.getJudgeFullName());

        return judge.equals(this);
    }
}
