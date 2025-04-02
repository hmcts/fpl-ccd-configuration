package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Jacksonized
@Data
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
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(getJudgeTitle())
            .otherTitle(getOtherTitle())
            .judgeLastName(getJudgeLastName())
            .judgeFullName(getJudgeFullName())
            .judgeEmailAddress(getJudgeEmailAddress())
            .build();
    }

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup, JudgeOrMagistrateTitle title) {
        String postNominals = isNotEmpty(jup.getPostNominals())
            ? (" " + jup.getPostNominals())
            : "";

        return Judge.builder()
            .judgeTitle((title == null) ? JudgeOrMagistrateTitle.OTHER : title)
            .otherTitle((title == null) ? jup.getTitle() : null)
            .judgeLastName(jup.getSurname() + postNominals)
            .judgeFullName(jup.getFullName() + postNominals)
            .judgeEmailAddress(jup.getEmailId())
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(jup.getSidamId())
                .personalCode(jup.getPersonalCode())
                .build())
            .build();
    }

    public static Judge fromJudicialUserProfile(JudicialUserProfile jup) {
        return fromJudicialUserProfile(jup, null);
    }
}
