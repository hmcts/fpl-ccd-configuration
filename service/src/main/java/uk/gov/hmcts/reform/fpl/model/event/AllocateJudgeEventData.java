package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.Temp;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllocateJudgeEventData {
    @Temp
    private final JudgeType judgeType;
    @Temp
    private final JudgeOrMagistrateTitle feePaidJudgeTitle;
    @Temp
    private final JudicialUser judicialUser;
    @Deprecated
    @Temp
    private final YesNo enterManually;
    @Temp
    private final Judge manualJudgeDetails;
}
