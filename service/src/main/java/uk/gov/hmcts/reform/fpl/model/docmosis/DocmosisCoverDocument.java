package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
public class DocmosisCoverDocument implements DocmosisData {
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;
    private final String representativeName;
    private final String representativeAddress;
    private final String logoLarge;
    private final String logoSmall;
}
