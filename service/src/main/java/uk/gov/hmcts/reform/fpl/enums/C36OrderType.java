package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "C36SupervisionOrderType", generate = true)
public enum C36OrderType {
    @CCD(label = "Variation of supervision order")
    VARIATION_OF_SUPERVISION_ORDER,
    @CCD(label = "Extension of supervision order")
    EXTENSION_OF_SUPERVISION_ORDER
}
