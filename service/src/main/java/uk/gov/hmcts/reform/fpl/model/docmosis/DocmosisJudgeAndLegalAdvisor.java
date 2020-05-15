package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DocmosisJudgeAndLegalAdvisor extends DocmosisJudge {
    private final String legalAdvisorName;
}
