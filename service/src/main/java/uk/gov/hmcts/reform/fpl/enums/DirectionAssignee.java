package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DirectionAssignee {
    @CCD(label = "All parties")
    ALL_PARTIES("allParties"),
    @CCD(label = "Local authority")
    LOCAL_AUTHORITY("localAuthorityDirections"),
    @CCD(label = "Parents and respondents")
    PARENTS_AND_RESPONDENTS("respondentDirections"),
    @CCD(label = "Cafcass")
    CAFCASS("cafcassDirections"),
    @CCD(label = "Others")
    OTHERS("otherPartiesDirections"),
    @CCD(label = "Court")
    COURT("courtDirections");

    private final String value;

    DirectionAssignee(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toHearingDateField() {
        return value.concat("HearingDate");
    }
}
