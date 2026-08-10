package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum JudicialMessageStatus {
    @CCD(label = "Open")
    OPEN,
    @CCD(label = "Closed")
    CLOSED
}
