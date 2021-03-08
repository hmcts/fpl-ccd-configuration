package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OtherApplicationType {
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION("C1 - Change surname or remove from jurisdiction"),
    C1_APPOINTMENT_OF_A_GUARDIAN("C1 - Appointment of a guardian"),
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN("C1 - Termination of appointment of a guardian"),
    C1_PARENTAL_RESPONSIBILITY("C1 - Parental responsibility"),
    C1_WITH_SUPPLEMENT("C1 - With supplement"),
    C4("C4 - Whereabouts of a missing child"),
    C12("C12 - Warrant to assist person authorised by an emergency protection order"),
    C17("C17 - Education supervision order (ESO)"),
    C17a("C17a - Variation or extension of ESO"),
    C19("C19 - Warrant of assistance"),
    C100("C100 - Child arrangements, prohibited steps or specific issue");

    private final String type;
}
