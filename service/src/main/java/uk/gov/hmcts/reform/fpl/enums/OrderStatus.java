package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum OrderStatus {
    @CCD(label = "Yes, seal it and send to the local authority")
    SEALED,
    @CCD(label = "No, just save it on the system")
    DRAFT,
    PLAIN // status for documents that will be generated to then store to be used under the slip rule
}
