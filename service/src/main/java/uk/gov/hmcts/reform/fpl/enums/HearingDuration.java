package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingDuration {
    DAYS("DAYS"),
    HOURS_MINS("HOURS_MINS"),
    DATE_TIME("DATE_TIME");

    private final String type;
}
