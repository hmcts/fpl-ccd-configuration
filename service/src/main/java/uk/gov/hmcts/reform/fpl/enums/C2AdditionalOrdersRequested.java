package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum C2AdditionalOrdersRequested {
    @CCD(label = "Change surname or remove from jurisdiction.")
    CHANGE_SURNAME_OR_REMOVE_JURISDICTION("Change surname or remove from jurisdiction"),
    @CCD(label = "Appointment of a guardian")
    APPOINTMENT_OF_GUARDIAN("Appointment of a guardian"),
    @CCD(label = "Termination of appointment of a guardian")
    TERMINATION_OF_APPOINTMENT_OF_GUARDIAN("Termination of appointment of a guardian"),
    @CCD(label = "Parental responsibility")
    PARENTAL_RESPONSIBILITY("Parental responsibility"),
    @CCD(label = "Requesting an adjournment for a scheduled hearing")
    REQUESTING_ADJOURNMENT("Requesting an adjournment for a scheduled hearing");

    private final String label;
}
