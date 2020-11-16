package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public abstract class AbstractJudge {

    private JudgeOrMagistrateTitle judgeTitle;
    private String otherTitle;
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

}

