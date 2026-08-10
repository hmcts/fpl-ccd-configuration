package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ParticularsOfChildren {
    @CCD(label = "in care")
    IN_CARE("in care"),
    @CCD(label = "the subject of an emergency protection order")
    SUBJECT_OF_EPO("the subject of an emergency protection order"),
    @CCD(label = "in police protection")
    IN_POLICE_PROTECTION("in police protection");

    private final String label;
}
