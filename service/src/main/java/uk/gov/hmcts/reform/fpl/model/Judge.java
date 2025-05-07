package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class Judge extends AbstractJudge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String judgeEmailAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final YesNo judgeEnterManually;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JudicialUser judgeJudicialUser;

    @Builder(toBuilder = true)
    private Judge(JudgeOrMagistrateTitle judgeTitle, String otherTitle, String judgeLastName,
                  String judgeFullName, String judgeEmailAddress, YesNo judgeEnterManually,
                  JudicialUser judgeJudicialUser) {
        super(judgeTitle, otherTitle, judgeLastName, judgeFullName, judgeEmailAddress, judgeEnterManually,
            judgeJudicialUser);
        this.judgeTitle = judgeTitle;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
        this.judgeEmailAddress = judgeEmailAddress;
        this.judgeEnterManually = judgeEnterManually;
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

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup, YesNo judgeEnterManually) {
        String postNominals = isNotEmpty(jup.getPostNominals())
            ? (" " + jup.getPostNominals())
            : "";

        return Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.OTHER)
            .otherTitle(jup.getTitle())
            .judgeLastName(jup.getSurname() + postNominals)
            .judgeFullName(jup.getFullName() + postNominals)
            .judgeEmailAddress(jup.getEmailId())
            .judgeEnterManually(judgeEnterManually)
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(jup.getSidamId())
                .personalCode(jup.getPersonalCode())
                .build())
            .build();
    }

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup) {
        return fromJudicialUserProfile(jup, YesNo.NO);
    }

}
