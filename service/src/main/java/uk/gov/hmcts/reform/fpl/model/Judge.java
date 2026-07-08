package uk.gov.hmcts.reform.fpl.model;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Objects;

@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Judge extends AbstractJudge {

    public boolean hasEqualJudgeFields(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return Objects.equals(getJudgeTitle(), judgeAndLegalAdvisor.getJudgeTitle())
            && StringUtils.equals(getOtherTitle(), judgeAndLegalAdvisor.getOtherTitle())
            && StringUtils.equals(getJudgeLastName(), judgeAndLegalAdvisor.getJudgeLastName())
            && StringUtils.equals(getJudgeFullName(), judgeAndLegalAdvisor.getJudgeFullName());
    }

    public JudgeAndLegalAdvisor toJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.from(this);
    }

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup, JudgeOrMagistrateTitle title) {
        return AbstractJudge.fromJudicialUserProfile(Judge.builder(), jup, title);
    }

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup) {
        return fromJudicialUserProfile(jup, null);
    }
}
