package uk.gov.hmcts.reform.fpl.enums;

public enum DirectionAssignee {
    ALL_PARTIES("allParties"),
    LOCAL_AUTHORITY("localAuthorityDirections"),
    PARENTS_AND_RESPONDENTS("respondentDirections"),
    CAFCASS("cafcassDirections"),
    OTHERS("otherPartiesDirections"),
    COURT("courtDirections");

    private final String value;

    DirectionAssignee(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toCustomDirectionField() {
        return value.concat("Custom");
    }

    public String toHearingDateField() {
        return value.concat("HearingDate");
    }
}
