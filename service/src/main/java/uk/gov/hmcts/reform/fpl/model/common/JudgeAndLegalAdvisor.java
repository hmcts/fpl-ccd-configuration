package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeAndLegalAdvisor extends AbstractJudge {
    @CCD(label = "Justices' Legal Adviser's full name")
    private final String legalAdvisorName;
    @CCD(label = " ")
    private String allocatedJudgeLabel;
    @CCD(
            label = "Is this judge issuing the order?",
            showCondition = "allocatedJudgeLabel!=\"\"",
            typeOverride = FieldType.YesOrNo
    )
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Who is issuing the order?", showCondition = "useAllocatedJudge=\"No\"", typeOverride = FieldType.Label)
  private String judgeSubHeading;
  // ==== end synthesised definition-only fields ====
}
