package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

public abstract class AbstractJudge {

    private JudgeOrMagistrateTitle judgeTitle;
    private String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;

    public AbstractJudge(JudgeOrMagistrateTitle title, String otherTitle, String judgeLastName, String judgeFullName) {
        this.judgeTitle = title;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
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

