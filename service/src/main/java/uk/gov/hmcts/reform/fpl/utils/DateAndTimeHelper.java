package uk.gov.hmcts.reform.fpl.utils;

import java.time.ZonedDateTime;

import static uk.gov.hmcts.reform.fpl.config.TimeConfiguration.LONDON_TIMEZONE;

public class DateAndTimeHelper {
    public static ZonedDateTime currentTimeUK() {
        return ZonedDateTime.now(LONDON_TIMEZONE);
    }
}
