package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum OtherPartiesDirectionAssignee {
    OTHER_1("Person 1"),
    OTHER_2("Other person 1"),
    OTHER_3("Other person 2"),
    OTHER_4("Other person 3"),
    OTHER_5("Other person 4"),
    OTHER_6("Other person 5"),
    OTHER_7("Other person 6"),
    OTHER_8("Other person 7"),
    OTHER_9("Other person 8"),
    OTHER_10("Other person 9");

    private final String label;

    OtherPartiesDirectionAssignee(String label) {
        this.label = label;
    }
}
