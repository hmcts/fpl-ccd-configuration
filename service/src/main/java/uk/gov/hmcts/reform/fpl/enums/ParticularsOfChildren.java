package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticularsOfChildren {
    IN_CARE("in care"),
    SUBJECT_OF_EPO("the subject of an emergency protection order"),
    IN_POLICE_PROTECTION("in police protection");

    private final String label;
}
