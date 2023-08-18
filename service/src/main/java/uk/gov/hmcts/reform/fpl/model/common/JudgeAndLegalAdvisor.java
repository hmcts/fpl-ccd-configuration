package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeAndLegalAdvisor extends AbstractJudge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String legalAdvisorName;
    private String allocatedJudgeLabel;
    private String useAllocatedJudge;
    private String judgeEmailAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private YesNo judgeEnterManually;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JudicialUser judgeJudicialUser;

    @Builder(toBuilder = true)
    @SuppressWarnings("java:S107")
    private JudgeAndLegalAdvisor(JudgeOrMagistrateTitle judgeTitle, String otherTitle, String judgeLastName,
                                 String judgeFullName, String legalAdvisorName, String allocatedJudgeLabel,
                                 String useAllocatedJudge, String judgeEmailAddress, YesNo judgeEnterManually,
                                 JudicialUser judgeJudicialUser) {
        super(judgeTitle, otherTitle, judgeLastName, judgeFullName, judgeEmailAddress, judgeEnterManually,
            judgeJudicialUser);
        this.judgeTitle = judgeTitle;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
        this.legalAdvisorName = legalAdvisorName;
        this.allocatedJudgeLabel = allocatedJudgeLabel;
        this.useAllocatedJudge = useAllocatedJudge;
        this.judgeEmailAddress = judgeEmailAddress;
        this.judgeEnterManually = judgeEnterManually;
        this.judgeJudicialUser = judgeJudicialUser;
    }

    @JsonIgnore
    public boolean isUsingAllocatedJudge() {
        return YES.getValue().equals(useAllocatedJudge);
    }

    public static JudgeAndLegalAdvisor from(final Judge allocatedJudge) {
        JudgeAndLegalAdvisorBuilder judgeAndLegalAdvisorBuilder = JudgeAndLegalAdvisor.builder();
        if (allocatedJudge != null) {
            judgeAndLegalAdvisorBuilder
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

    public static JudgeAndLegalAdvisor fromJudicialUserProfile(JudicialUserProfile jup) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.OTHER)
            .otherTitle(jup.getPostNominals())
            .judgeLastName(jup.getSurname())
            .judgeFullName(jup.getFullName())
            .judgeEmailAddress(jup.getEmailId())
            .judgeEnterManually(NO)
            .judgeJudicialUser(JudicialUser.builder()
                .idamId(jup.getSidamId())
                .personalCode(jup.getPersonalCode())
                .build())
            .build();
    }
}
