package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DraftOrderType", generate = true)
public enum HearingOrderType {
    @CCD(label = "Draft CMO from advocates' meeting")
    DRAFT_CMO,
    @CCD(label = "Agreed CMO discussed at hearing")
    AGREED_CMO,
    @CCD(label = "Draft order")
    C21;

    public boolean isCmo() {
        return this == DRAFT_CMO || this == AGREED_CMO;
    }
}
