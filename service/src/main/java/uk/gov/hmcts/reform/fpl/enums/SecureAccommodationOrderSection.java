package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecureAccommodationOrderSection {
    ENGLAND("Section 25 (England)"),
    WALES("Section 119 (Wales)");

    private final String label;
}