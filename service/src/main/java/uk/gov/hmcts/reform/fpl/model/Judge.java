package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class Judge extends AbstractJudge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String judgeEmailAddress;
    private final JudicialUser judgeJudicialUser;

    @Builder(toBuilder = true)
    private Judge(JudgeOrMagistrateTitle judgeTitle, String otherTitle, String judgeLastName,
                  String judgeFullName, String judgeEmailAddress, JudicialUser judgeJudicialUser) {
        super(judgeTitle, otherTitle, judgeLastName, judgeFullName, judgeEmailAddress, judgeJudicialUser);
        this.judgeTitle = judgeTitle;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
        this.judgeEmailAddress = judgeEmailAddress;
        this.judgeJudicialUser = judgeJudicialUser;
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

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup) {
        return Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.OTHER)
            .otherTitle(jup.getPostNominals())
            .judgeLastName(jup.getSurname())
            .judgeFullName(jup.getFullName())
            .judgeEmailAddress(jup.getEmailId())
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(jup.getSidamId())
                .personalCode(jup.getPersonalCode())
                .build())
            .build();
    }
}
