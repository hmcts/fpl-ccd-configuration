package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum HearingStatus {
    @CCD(label = "Adjourned")
    ADJOURNED,
    @CCD(label = "Adjourned")
    ADJOURNED_AND_RE_LISTED,
    @CCD(label = "Adjourned - to be re-listed")
    ADJOURNED_TO_BE_RE_LISTED,
    @CCD(label = "Vacated")
    VACATED,
    @CCD(label = "Vacated")
    VACATED_AND_RE_LISTED,
    @CCD(label = "Vacated - to be re-listed")
    VACATED_TO_BE_RE_LISTED
}
