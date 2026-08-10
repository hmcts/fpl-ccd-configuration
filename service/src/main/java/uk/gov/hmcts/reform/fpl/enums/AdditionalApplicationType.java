package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum AdditionalApplicationType {
    @CCD(label = "C2 - to add or remove someone on a case, or for a specific request to the judge")
    C2_ORDER,
    @CCD(label = "Other specific order - including C1 and C100 orders, and supplements")
    OTHER_ORDER;
}
