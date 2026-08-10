package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CMOStatus {
    @CCD(label = "Yes, send this to the judge")
    SEND_TO_JUDGE,
    APPROVED,
    RETURNED,
    DRAFT,
    REMOVED
}
