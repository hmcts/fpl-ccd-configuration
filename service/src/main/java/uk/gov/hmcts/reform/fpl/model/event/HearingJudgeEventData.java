package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(force = true)
public class HearingJudgeEventData extends AllocateJudgeEventData {
    @JsonCreator
    public HearingJudgeEventData(@JsonProperty("hearingJudgeType") JudgeType judgeType,
                                 @JsonProperty("hearingFeePaidJudgeTitle") JudgeOrMagistrateTitle feePaidJudgeTitle,
                                 @JsonProperty("hearingJudicialUser") JudicialUser judicialUser,
                                 @JsonProperty("hearingManualJudgeDetails") Judge manualJudgeDetails) {
        super(judgeType, feePaidJudgeTitle, judicialUser, manualJudgeDetails);
    }

}
