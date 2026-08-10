package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DirectionDueDateType {
    @CCD(label = "Date and time")
    DATE,
    @CCD(label = "Number of working days before hearing")
    DAYS
}
