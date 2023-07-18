package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;

import static org.springframework.util.ObjectUtils.isEmpty;
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
    private final YesNo judgeEnterManually;
    private final JudicialUser judgeJudicialUser;


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

    public YesNo getJudgeEnterManually() {
        return !isEmpty(this.judgeEnterManually) ? this.judgeEnterManually : YesNo.YES;
    }
}

