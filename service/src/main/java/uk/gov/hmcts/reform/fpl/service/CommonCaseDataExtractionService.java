package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;

@Service
public class CommonCaseDataExtractionService {

    String getHearingTime(HearingBooking hearingBooking) {
        String hearingTime;
        final LocalDateTime startDate = hearingBooking.getStartDate();
        final LocalDateTime endDate = hearingBooking.getEndDate();

        if (hearingBooking.hasDatesOnSameDay()) {
            // Example 3:30pm - 5:30pm
            hearingTime = String.format("%s - %s",
                formatTime(startDate), formatTime(endDate));
        } else {
            // Example 18 June, 3:40pm - 19 June, 2:30pm
            hearingTime = String.format("%s - %s", formatDateTime(startDate),
                formatDateTime(endDate));
        }

        return hearingTime;
    }

    Optional<String> getHearingDateIfHearingsOnSameDay(HearingBooking hearingBooking) {
        return Optional.of(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG));
    }

    String extractPrehearingAttendance(HearingBooking booking) {
        LocalDateTime time = calculatePrehearingAttendance(booking.getStartDate());

        return booking.hasDatesOnSameDay() ? formatTime(time) : formatDateTimeWithYear(time);
    }

    private LocalDateTime calculatePrehearingAttendance(LocalDateTime dateTime) {
        return dateTime.minusHours(1);
    }

    private String formatDateTimeWithYear(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM yyyy, h:mma");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM, h:mma");
    }

    private String formatTime(LocalDateTime dateTime) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, "h:mma");
    }
}
