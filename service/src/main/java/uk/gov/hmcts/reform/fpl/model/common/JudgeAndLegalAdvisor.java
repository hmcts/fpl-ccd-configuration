package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeAndLegalAdvisor extends AbstractJudge {
    private final String legalAdvisorName;
    private String allocatedJudgeLabel;
    private String useAllocatedJudge;

    @JsonIgnore
    public boolean isUsingAllocatedJudge() {
        return YES.getValue().equals(useAllocatedJudge);
    }

    public static JudgeAndLegalAdvisor from(final Judge allocatedJudge) {
        JudgeAndLegalAdvisorBuilder<?, ?> judgeAndLegalAdvisorBuilder = JudgeAndLegalAdvisor.builder();
        if (allocatedJudge != null) {
            judgeAndLegalAdvisorBuilder
                .judgeType(allocatedJudge.getJudgeType())
                .judgeTitle(allocatedJudge.getJudgeTitle())
                .otherTitle(allocatedJudge.getOtherTitle())
                .judgeLastName(allocatedJudge.getJudgeLastName())
                .judgeFullName(allocatedJudge.getJudgeFullName())
                .judgeEnterManually(allocatedJudge.getJudgeEnterManually())
                .judgeJudicialUser(allocatedJudge.getJudgeJudicialUser())
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

    public static JudgeAndLegalAdvisor fromJudicialUserProfile(JudicialUserProfile jup,
                                                               JudgeOrMagistrateTitle title) {
        return AbstractJudge.fromJudicialUserProfile(JudgeAndLegalAdvisor.builder(), jup, title);
    }
}
