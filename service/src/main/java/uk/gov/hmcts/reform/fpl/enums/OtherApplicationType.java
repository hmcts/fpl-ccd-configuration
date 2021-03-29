package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OtherApplicationType {
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION("C1", 1),
    C1_APPOINTMENT_OF_A_GUARDIAN("C1", 1),
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN("C1", 1),
    C1_PARENTAL_RESPONSIBILITY("C1", 1),
    C1_WITH_SUPPLEMENT("C1", 1),
    C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD("C3", 3),
    C4_WHEREABOUTS_OF_A_MISSING_CHILD("C4", 4),
    C12_WARRANT_TO_ASSIST_PERSON("C12", 12),
    C17_EDUCATION_SUPERVISION_ORDER("C17", 17),
    C17A_EXTENSION_OF_ESO("C17a", 17),
    C19_WARRANT_TO_ASSISTANCE("C19", 19),
    C100_CHILD_ARRANGEMENTS("C100", 100);

    private final String type;
    private final int sortOrder;
}
