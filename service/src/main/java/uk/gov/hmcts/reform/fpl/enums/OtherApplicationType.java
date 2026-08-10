package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@AllArgsConstructor
@Getter
public enum OtherApplicationType {
    @CCD(label = "C1 - Change surname or remove from jurisdiction")
    C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION("C1 - Change surname or remove from jurisdiction", 1),
    @CCD(label = "C1 - Appointment of a guardian")
    C1_APPOINTMENT_OF_A_GUARDIAN("C1 - Appointment of a guardian", 1),
    @CCD(label = "C1 - Termination of appointment of a guardian")
    C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN("C1 - Termination of appointment of a guardian", 1),
    @CCD(label = "C1 - Parental responsibility")
    C1_PARENTAL_RESPONSIBILITY("C1 - Parental responsibility", 1),
    @CCD(label = "C1 - With supplement")
    C1_WITH_SUPPLEMENT("C1 - With supplement", 1),
    @CCD(label = "C3 - Search, take charge and delivery of a child")
    C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD("C3 - Search, take charge and delivery of a child", 3),
    @CCD(label = "C4 - Whereabouts of a missing child")
    C4_WHEREABOUTS_OF_A_MISSING_CHILD("C4 - Whereabouts of a missing child", 4),
    @CCD(label = "C12 - Warrant to assist person authorised by an emergency protection order")
    C12_WARRANT_TO_ASSIST_PERSON("C12 - Warrant to assist person authorised by an emergency protection order", 12),
    @CCD(label = "C17 - Education supervision order (ESO)")
    C17_EDUCATION_SUPERVISION_ORDER("C17 - Education supervision order (ESO)", 17),
    @CCD(label = "C17a - Variation or extension of ESO")
    C17A_EXTENSION_OF_ESO("C17a - Variation or extension of ESO", 17),
    @CCD(label = "C19 - Warrant of assistance")
    C19_WARRANT_TO_ASSISTANCE("C19 - Warrant of assistance", 19),
    @CCD(label = "C63 - Declaration of parentage")
    C63_DECLARATION_OF_PARENTAGE("C63 - Declaration of parentage", 63),
    @CCD(label = "C100 - Child arrangements, prohibited steps or specific issue")
    C100_CHILD_ARRANGEMENTS("C100 - Child arrangements, prohibited steps or specific issue", 100);

    private final String label;
    private final int sortOrder;
}
