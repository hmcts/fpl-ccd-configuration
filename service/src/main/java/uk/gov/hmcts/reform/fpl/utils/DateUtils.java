package uk.gov.hmcts.reform.fpl.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {

    }

    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
