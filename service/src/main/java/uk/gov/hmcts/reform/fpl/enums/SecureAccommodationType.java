package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@AllArgsConstructor
@Getter
public enum SecureAccommodationType {
    @CCD(label = "England")
    ENGLAND("England"),
    @CCD(label = "Wales")
    WALES("Wales");

    private final String label;
}
