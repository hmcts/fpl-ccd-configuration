package uk.gov.hmcts.reform.fpl.enums.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingUrgencyType {
    STANDARD("Standard (between days 12-18)"),
    SAME_DAY("Same day"),
    URGENT("Urgent (not same day)");

    private final String label;
}
