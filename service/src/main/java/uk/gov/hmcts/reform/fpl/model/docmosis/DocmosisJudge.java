package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class DocmosisJudge {
    private final String judgeTitleAndName;
}
