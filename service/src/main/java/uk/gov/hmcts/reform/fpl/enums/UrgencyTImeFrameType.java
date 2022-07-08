package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrgencyTImeFrameType {
    SAME_DAY("Same day", 0),
    WITHIN_2_DAYS("Within 2 days", 2),
    WITHIN_5_DAYS("Within 5 days", 5);

    private final String label;
    private final int count;
}
