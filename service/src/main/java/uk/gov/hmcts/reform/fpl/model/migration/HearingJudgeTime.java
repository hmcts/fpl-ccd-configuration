package uk.gov.hmcts.reform.fpl.model.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingJudgeTime {

    private String emailAddress;
    private String judgeId;
    private JudgeOrMagistrateTitle judgeType;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

}
