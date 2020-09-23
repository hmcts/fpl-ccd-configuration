package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DocmosisJudgeAndLegalAdvisor extends DocmosisJudge {
    private final String legalAdvisorName;
}
