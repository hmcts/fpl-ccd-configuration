package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;
import uk.gov.hmcts.reform.fpl.enums.interfaces.Assignee;

@Getter
public enum ParentsAndRespondentsDirectionAssignee implements Assignee, HasLabel {
    RESPONDENT_1("Respondent 1"),
    RESPONDENT_2("Respondent 2"),
    RESPONDENT_3("Respondent 3"),
    RESPONDENT_4("Respondent 4"),
    RESPONDENT_5("Respondent 5"),
    RESPONDENT_6("Respondent 6"),
    RESPONDENT_7("Respondent 7"),
    RESPONDENT_8("Respondent 8"),
    RESPONDENT_9("Respondent 9"),
    RESPONDENT_10("Respondent 10");

    private final String label;

    ParentsAndRespondentsDirectionAssignee(String label) {
        this.label = label;
    }
}
