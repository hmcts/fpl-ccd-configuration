package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingHousekeepReason {
    LIST_IN_ERROR("Listed in error"),
    DUPLICATE("Duplicate hearing event"),
    OTHER("Other");

    private final String label;
}
