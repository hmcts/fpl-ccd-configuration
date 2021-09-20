package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Judge;

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

    @Builder(toBuilder = true)
    @SuppressWarnings("java:S107")
    private JudgeAndLegalAdvisor(JudgeOrMagistrateTitle judgeTitle, String otherTitle, String judgeLastName,
                                 String judgeFullName, String legalAdvisorName, String allocatedJudgeLabel,
                                 String useAllocatedJudge, String judgeEmailAddress) {
        super(judgeTitle, otherTitle, judgeLastName, judgeFullName, judgeEmailAddress);
        this.judgeTitle = judgeTitle;
        this.otherTitle = otherTitle;
        this.judgeLastName = judgeLastName;
        this.judgeFullName = judgeFullName;
        this.legalAdvisorName = legalAdvisorName;
        this.allocatedJudgeLabel = allocatedJudgeLabel;
        this.useAllocatedJudge = useAllocatedJudge;
        this.judgeEmailAddress = judgeEmailAddress;
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
}
