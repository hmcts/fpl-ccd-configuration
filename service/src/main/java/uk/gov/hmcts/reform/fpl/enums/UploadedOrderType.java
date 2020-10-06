package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UploadedOrderType {
    C24("Variation of Emergency protection order"),
    C25("Warrant to assist EPO"),
    C27("Authority to search for another child"),
    C28("Warrant to assist"),
    C29("Recovery of a child"),
    C30("To disclose information about the whereabouts of a missing child"),
    C31("Authority to search for a child"),
    C34A("Contact with a child in care"),
    C34B("Refusal of contact with a child in care"),
    C36("Variation/extension of Education supervision order"),
    C37("Education supervision order", true),
    C38A("Discharge education supervision order"),
    C38B("Extension of an education supervision order"),
    C39("Child assessment order"),
    C42("Family assistance order"),
    C43("Child arrangements/Specific issue/Prohibited steps order (including interim orders)"),
    C43A("Special guardianship order"),
    C44A("Leave to change surname"),
    C44B("Leave to remove a child from the UK"),
    C45A("Parental responsibility order"),
    C45B("Discharge of parental responsibility"),
    C46A("Appointment of a guardian"),
    C46B("Termination of guardian's appointment"),
    C47A("Appointment of a children's guardian"),
    C47B("Refusal of appointment of a children's guardian"),
    C47C("Termination of appointment of a children's guardian"),
    C48A("Appointment of a solicitor"),
    C48B("Refusal of appointment of a solicitor"),
    C48C("Termination of appointment of a solicitor"),
    C49("Transfer out Children Act"),
    C50("Refusal to transfer proceedings"),
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
