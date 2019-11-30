package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

@Data
@Builder
public class JudgeAndLegalAdvisor {
    private final JudgeOrMagistrateTitle judgeTitle;
    private final String otherTitle;
    private final String judgeLastName;
    private final String judgeFullName;
    private final String legalAdvisorName;
}
