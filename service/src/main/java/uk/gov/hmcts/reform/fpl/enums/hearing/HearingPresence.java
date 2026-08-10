package uk.gov.hmcts.reform.fpl.enums.hearing;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Deprecated
public enum HearingPresence {
    @CCD(label = "Remote")
    REMOTE,
    @CCD(label = "In person")
    IN_PERSON
}
