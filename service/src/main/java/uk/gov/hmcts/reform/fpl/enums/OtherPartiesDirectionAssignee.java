package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.interfaces.Assignee;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum OtherPartiesDirectionAssignee implements Assignee {
    @CCD(label = "Person 1")
    OTHER_1("Person 1"),
    @CCD(label = "Other person 1")
    OTHER_2("Other person 1"),
    @CCD(label = "Other person 2")
    OTHER_3("Other person 2"),
    @CCD(label = "Other person 3")
    OTHER_4("Other person 3"),
    @CCD(label = "Other person 4")
    OTHER_5("Other person 4"),
    @CCD(label = "Other person 5")
    OTHER_6("Other person 5"),
    @CCD(label = "Other person 6")
    OTHER_7("Other person 6"),
    @CCD(label = "Other person 7")
    OTHER_8("Other person 7"),
    @CCD(label = "Other person 8")
    OTHER_9("Other person 8"),
    @CCD(label = "Other person 9")
    OTHER_10("Other person 9");

    private final String label;

    OtherPartiesDirectionAssignee(String label) {
        this.label = label;
    }
}
