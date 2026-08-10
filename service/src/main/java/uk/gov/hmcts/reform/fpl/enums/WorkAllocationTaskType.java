package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum WorkAllocationTaskType {
    @CCD(label = "Failed Payment")
    FAILED_PAYMENT,
    @CCD(label = "Order not uploaded")
    ORDER_NOT_UPLOADED,
    @CCD(label = "Order uploaded")
    ORDER_UPLOADED,
    @CCD(label = "Correspondence Uploaded")
    CORRESPONDENCE_UPLOADED,
    @CCD(label = "Case Management Order reviewed")
    CMO_REVIEWED
}
