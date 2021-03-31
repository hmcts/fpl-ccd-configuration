package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum C2AdditionalOrdersRequested {
    CHANGE_SURNAME_OR_REMOVE_JURISDICTION("Change surname or remove from jurisdiction"),
    APPOINTMENT_OF_GUARDIAN("Appointment of a guardian"),
    TERMINATION_OF_APPOINTMENT_OF_GUARDIAN("Termination of appointment of a guardian"),
    PARENTAL_RESPONSIBILITY("Parental responsibility");

    private final String label;
}
