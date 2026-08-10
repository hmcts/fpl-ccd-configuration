package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DraftOrderKind", generate = true)
public enum HearingOrderKind {
    @CCD(label = "Case Management (CMO)")
    CMO,
    @CCD(label = "Additional order (PDO ETC)")
    C21;
}
