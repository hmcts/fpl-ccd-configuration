package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum RepresentativeServingPreferences {
    @CCD(label = "By post")
    POST,
    @CCD(label = "By email")
    EMAIL,
    @CCD(label = "Through the digital service")
    DIGITAL_SERVICE
}
