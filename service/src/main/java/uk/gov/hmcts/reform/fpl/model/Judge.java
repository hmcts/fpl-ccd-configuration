package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class Judge extends AbstractJudge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String judgeEmailAddress;

    @Builder(toBuilder = true)
    private Judge(JudgeOrMagistrateTitle judgeTitle, String otherTitle, String judgeLastName,
                  String judgeFullName, String judgeEmailAddress) {
        super(judgeTitle, otherTitle, judgeLastName, judgeFullName);
        this.judgeTitle = judgeTitle;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
        this.judgeEmailAddress = judgeEmailAddress;
    }

    public boolean hasEqualJudgeFields(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return Objects.equals(getJudgeTitle(), judgeAndLegalAdvisor.getJudgeTitle())
            && StringUtils.equals(getOtherTitle(), judgeAndLegalAdvisor.getOtherTitle())
            && StringUtils.equals(getJudgeLastName(), judgeAndLegalAdvisor.getJudgeLastName())
            && StringUtils.equals(getJudgeFullName(), judgeAndLegalAdvisor.getJudgeFullName());
    }

    public JudgeAndLegalAdvisor toJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(judgeTitle)
            .otherTitle(otherTitle)
            .judgeLastName(judgeLastName)
            .judgeFullName(judgeFullName)
            .judgeEmailAddress(judgeEmailAddress)
            .build();
    }
}
