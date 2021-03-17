package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SecureAccommodationType {
    ENGLAND("England"),
    WALES("Wales");

    private final String label;
}
