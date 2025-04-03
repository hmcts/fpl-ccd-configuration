package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_HIGH_COURT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.RECORDER;

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
}
