package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.interfaces.Assignee;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum ParentsAndRespondentsDirectionAssignee implements Assignee {
    @CCD(label = "Respondent 1")
    RESPONDENT_1("Respondent 1"),
    @CCD(label = "Respondent 2")
    RESPONDENT_2("Respondent 2"),
    @CCD(label = "Respondent 3")
    RESPONDENT_3("Respondent 3"),
    @CCD(label = "Respondent 4")
    RESPONDENT_4("Respondent 4"),
    @CCD(label = "Respondent 5")
    RESPONDENT_5("Respondent 5"),
    @CCD(label = "Respondent 6")
    RESPONDENT_6("Respondent 6"),
    @CCD(label = "Respondent 7")
    RESPONDENT_7("Respondent 7"),
    @CCD(label = "Respondent 8")
    RESPONDENT_8("Respondent 8"),
    @CCD(label = "Respondent 9")
    RESPONDENT_9("Respondent 9"),
    @CCD(label = "Respondent 10")
    RESPONDENT_10("Respondent 10");

    private final String label;

    ParentsAndRespondentsDirectionAssignee(String label) {
        this.label = label;
    }
}
