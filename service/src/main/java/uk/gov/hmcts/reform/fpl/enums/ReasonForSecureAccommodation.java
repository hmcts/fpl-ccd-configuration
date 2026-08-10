package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ReasonsForSecureAccommodation", generate = true)
public enum ReasonForSecureAccommodation {

    @CCD(label = "abscond and suffer harm")
    ABSCOND,
    @CCD(label = "injure themselves or others")
    INJURY

}
