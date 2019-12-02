package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum OtherPartiesDirectionAssignee {
    PERSON_1("Person 1"),
    OTHER_PERSON_1("Other Person 1"),
    OTHER_PERSON_2("Other Person 2"),
    OTHER_PERSON_3("Other Person 3"),
    OTHER_PERSON_4("Other Person 4"),
    OTHER_PERSON_5("Other Person 5"),
    OTHER_PERSON_6("Other Person 6"),
    OTHER_PERSON_7("Other Person 7"),
    OTHER_PERSON_8("Other Person 8"),
    OTHER_PERSON_9("Other Person 9");

    private final String label;

    OtherPartiesDirectionAssignee(String label) {
        this.label = label;
    }
}
