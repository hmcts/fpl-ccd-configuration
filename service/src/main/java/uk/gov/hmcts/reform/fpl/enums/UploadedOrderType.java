package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@AllArgsConstructor
@Getter
public enum UploadedOrderType {
    @CCD(label = "Other")
    OTHER(null),
    @CCD(label = "Variation of Emergency protection order - C24")
    C24("Variation of Emergency protection order"),
    @CCD(label = "Warrant to assist EPO - C25")
    C25("Warrant to assist EPO"),
    @CCD(label = "Authority to search for another child - C27")
    C27("Authority to search for another child"),
    @CCD(label = "Warrant to assist - C28")
    C28("Warrant to assist"),
    @CCD(label = "Recovery of a child - C29")
    C29("Recovery of a child"),
    @CCD(label = "To disclose information about the whereabouts of a missing child - C30")
    C30("To disclose information about the whereabouts of a missing child"),
    @CCD(label = "Authority to search for a child - C31")
    C31("Authority to search for a child"),
    @CCD(label = "Contact with a child in care - C34A")
    C34A("Contact with a child in care"),
    @CCD(label = "Refusal of contact with a child in care - C34B")
    C34B("Refusal of contact with a child in care"),
    @CCD(label = "Variation/extension of Education supervision order - C36")
    C36("Variation/extension of Education supervision order"),
    @CCD(label = "Education supervision order - C37")
    C37("Education supervision order", true),
    @CCD(label = "Discharge education supervision order - C38A")
    C38A("Discharge education supervision order"),
    @CCD(label = "Extension of an education supervision order - C38B")
    C38B("Extension of an education supervision order"),
    @CCD(label = "Family assistance order - C42")
    C42("Family assistance order"),
    @CCD(label = "Child arrangements/Specific issue/Prohibited steps order (including interim orders) - C43")
    C43("Child arrangements/Specific issue/Prohibited steps order (including interim orders)"),
    @CCD(label = "Special guardianship order - C43A")
    C43A("Special guardianship order"),
    @CCD(label = "Leave to change surname - C44A")
    C44A("Leave to change surname"),
    @CCD(label = "Leave to remove a child from the UK - C44B")
    C44B("Leave to remove a child from the UK"),
    @CCD(label = "Parental responsibility order - C45A")
    C45A("Parental responsibility order"),
    @CCD(label = "Discharge of parental responsibility - C45B")
    C45B("Discharge of parental responsibility"),
    @CCD(label = "Appointment of a guardian - C46A")
    C46A("Appointment of a guardian"),
    @CCD(label = "Termination of guardian's appointment - C46B")
    C46B("Termination of guardian's appointment"),
    @CCD(label = "Appointment of a children's guardian - C47A")
    C47A("Appointment of a children's guardian"),
    @CCD(label = "Refusal of appointment of a children's guardian - C47B")
    C47B("Refusal of appointment of a children's guardian"),
    @CCD(label = "Termination of appointment of a children's guardian - C47C")
    C47C("Termination of appointment of a children's guardian"),
    @CCD(label = "Appointment of a solicitor - C48A")
    C48A("Appointment of a solicitor"),
    @CCD(label = "Refusal of appointment of a solicitor - C48B")
    C48B("Refusal of appointment of a solicitor"),
    @CCD(label = "Termination of appointment of a solicitor - C48C")
    C48C("Termination of appointment of a solicitor"),
    @CCD(label = "Transfer out Children Act - C49")
    C49("Transfer out Children Act"),
    @CCD(label = "Refusal to transfer proceedings - C50")
    C50("Refusal to transfer proceedings"),
    @CCD(label = "Power of arrest - FL406")
    FL406("Power of arrest");

    UploadedOrderType(String label) {
        this(label, false);
    }

    private final String label;
    private final boolean isFinal;

    public String getFullLabel() {
        return String.format("%s (%s)", getLabel(), name());
    }
}
