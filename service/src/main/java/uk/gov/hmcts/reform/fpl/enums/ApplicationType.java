package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    C2_APPLICATION("C2"),
    C110A_APPLICATION("C110a"),
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION("C1 - Change surname or remove from jurisdiction"),
    C1_APPOINTMENT_OF_A_GUARDIAN("C1 - Appointment of a guardian"),
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN("C1 - Termination of appointment of a guardian"),
    C1_PARENTAL_RESPONSIBILITY("C1 - Parental responsibility"),
    C1_WITH_SUPPLEMENT("C1 - With supplement"),
    C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD("C3 - Search, take charge and delivery of a child"),
    C4_WHEREABOUTS_OF_A_MISSING_CHILD("C4 - Whereabouts of a missing child"),
    C12_WARRANT_TO_ASSIST_PERSON("C12 - Warrant to assist person authorised by an emergency protection order"),
    C17_EDUCATION_SUPERVISION_ORDER("C17 - Education supervision order (ESO)"),
    C17A_EXTENSION_OF_ESO("C17a - Variation or extension of ESO"),
    C19_WARRANT_TO_ASSISTANCE("C19 - Warrant of assistance"),
    C63_DECLARATION_OF_PARENTAGE("C63 - Declaration of parentage"),
    C100_CHILD_ARRANGEMENTS("C100 - Child arrangements, prohibited steps or specific issue");

    private final String type;
}
