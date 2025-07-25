package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.Temp;

import static uk.gov.hmcts.reform.fpl.enums.JudgeType.FEE_PAID_JUDGE;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AllocateJudgeEventData {
    @Temp
    private final JudgeType judgeType;
    @Temp
    private final JudgeOrMagistrateTitle feePaidJudgeTitle;
    @Temp
    private final JudicialUser judicialUser;
    @Temp
    private final Judge manualJudgeDetails;

    public JudgeOrMagistrateTitle getFeePaidJudgeTitle() {
        return (FEE_PAID_JUDGE.equals(judgeType)) ? feePaidJudgeTitle : null;
    }
}
