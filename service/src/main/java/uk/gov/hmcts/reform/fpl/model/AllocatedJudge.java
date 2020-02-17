package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

@Data
@Builder
@AllArgsConstructor
public class AllocatedJudge {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
}
