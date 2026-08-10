package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum HearingDuration {
    @CCD(label = "Set number of days")
    DAYS("DAYS"),
    @CCD(label = "Set number of hours and minutes")
    HOURS_MINS("HOURS_MINS"),
    @CCD(label = "Specific end date and time")
    DATE_TIME("DATE_TIME");

    private final String type;
}
