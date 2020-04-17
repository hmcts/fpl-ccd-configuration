package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(builderClassName = "Builder")
public class DocmosisCoverDocument extends DocmosisData {
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;
    private final String representativeName;
    private final String representativeAddress;
}
