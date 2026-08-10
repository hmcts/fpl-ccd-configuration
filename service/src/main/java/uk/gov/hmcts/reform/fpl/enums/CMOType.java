package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CMOType {
    @CCD(label = "Agreed CMO discussed at hearing - judge to check and seal")
    AGREED,
    @CCD(label = "Draft CMO from advocates' meeting - judge to review before hearing")
    DRAFT
}
