package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UploadableOrderType {
    C24("Variation of Emergency protection order - C24"),
    C25("Warrant to assist EPO - C25"),
    C27("Authority to search for another child - C27"),
    C28("Warrant to assist - C28"),
    C29("Recovery of a child - C29"),
    C30("To disclose information about the whereabouts of a missing child - C30"),
    C31("Authority to search for a child - C31"),
    C34A("Contact with a child in care - C34A"),
    C34B("Refusal of contact with a child in care - C34B"),
    C36("Variation/extension of Education supervision order - C36"),
    C37("Education supervision order - C37", true),
    C38A("Discharge education supervision order - C38A"),
    C38B("Extension of an education supervision order - C38B"),
    C39("Child assessment order - C39"),
    C42("Family assistance order - C42"),
    C43("Child arrangements/Specific issue/Prohibited steps order (including interim orders) - C43"),
    C43A("Special guardianship order - C43A"),
    C44A("Leave to change surname - C44A"),
    C44B("Leave to remove a child from the UK - C44B"),
    C45A("Parental responsibility order - C45A"),
    C45B("Discharge of parental responsibility - C45B"),
    C46A("Appointment of a guardian - C46A"),
    C46B("Termination of guardian's appointment - C46B"),
    C47A("Appointment of a children's guardian - C47A"),
    C47B("Refusal of appointment of a children's guardian - C47B"),
    C47C("Termination of appointment of a children's guardian - C47C"),
    C48A("Appointment of a solicitor - C48A"),
    C48B("Refusal of appointment of a solicitor - C48B"),
    C48C("Termination of appointment of a solicitor - C48C"),
    C49("Transfer out Children Act - C49"),
    C50("Refusal to transfer proceedings - C50"),
    FL406("Power of arrest - FL406");

    UploadableOrderType(String label) {
        this(label, false);
    }

    private final String label;
    private final boolean isFinal;
}
