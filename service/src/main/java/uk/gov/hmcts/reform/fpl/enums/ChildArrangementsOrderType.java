package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ChildArrangementsOrderType {
    @CCD(label = "Child to live with")
    CHILD_LIVE,
    @CCD(label = "Child to have contact with")
    CHILD_CONTACT
}
