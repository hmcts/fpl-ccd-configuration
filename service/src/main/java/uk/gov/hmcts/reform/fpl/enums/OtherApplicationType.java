package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OtherApplicationType {
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION("C1 - Change surname or remove from jurisdiction", 1),
    C1_APPOINTMENT_OF_A_GUARDIAN("C1 - Appointment of a guardian", 1),
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN("C1 - Termination of appointment of a guardian", 1),
    C1_PARENTAL_RESPONSIBILITY("C1 - Parental responsibility", 1),
    C1_WITH_SUPPLEMENT("C1 - With supplement", 1),
    C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD("C3 - Search, take charge and delivery of a child", 3),
    C4_WHEREABOUTS_OF_A_MISSING_CHILD("C4 - Whereabouts of a missing child", 4),
    C12_WARRANT_TO_ASSIST_PERSON("C12 - Warrant to assist person authorised by an emergency protection order", 12),
    C17_EDUCATION_SUPERVISION_ORDER("C17 - Education supervision order (ESO)", 17),
    C17A_EXTENSION_OF_ESO("C17a - Variation or extension of ESO", 17),
    C19_WARRANT_TO_ASSISTANCE("C19 - Warrant of assistance", 19),
    C63_DECLARATION_OF_PARENTAGE("C63 - Declaration of parentage", 63),
    C100_CHILD_ARRANGEMENTS("C100 - Child arrangements, prohibited steps or specific issue", 100);

    private final String label;
    private final int sortOrder;
}
