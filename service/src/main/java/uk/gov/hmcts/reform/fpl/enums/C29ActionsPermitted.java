package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum C29ActionsPermitted {
    @CCD(label = "Entry")
    ENTRY,
    @CCD(label = "Inform")
    INFORM,
    @CCD(label = "Produce")
    PRODUCE,
    @CCD(label = "Remove")
    REMOVE
}
