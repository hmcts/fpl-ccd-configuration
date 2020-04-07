package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisJudgeAndLegalAdvisor {
    private final String judgeTitleAndName;
    private final String legalAdvisorName;
}
