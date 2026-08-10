package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum RemovableType {
    @CCD(label = "Order")
    ORDER,
    @CCD(label = "Main Application Form")
    APPLICATION,
    @CCD(label = "Additional Application")
    ADDITIONAL_APPLICATION,
    @CCD(label = "Document sent to parties")
    SENT_DOCUMENT,
    @CCD(label = "Remove Placement Applications only")
    PLACEMENT_APPLICATION;
}
