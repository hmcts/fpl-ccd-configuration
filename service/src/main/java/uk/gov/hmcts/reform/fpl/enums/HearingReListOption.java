package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum HearingReListOption {
    @CCD(label = "Yes - and I can add the new date now")
    RE_LIST_NOW,
    @CCD(label = "Yes - but I do not have the new date yet")
    RE_LIST_LATER,
    @CCD(label = "No - it’s not being re-listed")
    NONE
}
