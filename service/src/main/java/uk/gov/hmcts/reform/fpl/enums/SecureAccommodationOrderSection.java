package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SecureAccommodationOrderSections", generate = true)
@Getter
@RequiredArgsConstructor
public enum SecureAccommodationOrderSection {
    @CCD(label = "Section 25 (England)")
    ENGLAND("Section 25 (England)"),
    @CCD(label = "Section 119 (Wales)")
    WALES("Section 119 (Wales)");

    private final String label;
}