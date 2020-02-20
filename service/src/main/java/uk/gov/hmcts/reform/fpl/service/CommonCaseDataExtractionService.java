package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
public class CommonCaseDataExtractionService {
    private final DateFormatterService dateFormatterService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    public static final String HEARING_EMPTY_PLACEHOLDER = "This will appear on the issued CMO";

    @Autowired
    public CommonCaseDataExtractionService(DateFormatterService dateFormatterService,
                                           HearingVenueLookUpService hearingVenueLookUpService) {
        this.dateFormatterService = dateFormatterService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
    }

    public String getHearingTime(HearingBooking hearingBooking) {
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

    public Optional<String> getHearingDateIfHearingsOnSameDay(HearingBooking hearingBooking) {
        String hearingDate = null;

        // If they aren't on the same date return nothing
        if (hearingBooking.hasDatesOnSameDay()) {
            hearingDate = dateFormatterService.formatLocalDateToString(
                hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG);
        }

        return Optional.ofNullable(hearingDate);
    }

    // NOTE: doesn't get anything to do with judge
    public Map<String, Object> getHearingBookingData(final HearingBooking hearingBooking) {
        if (hearingBooking == null) {
            return ImmutableMap.of(
                "hearingDate", HEARING_EMPTY_PLACEHOLDER,
                "hearingVenue", HEARING_EMPTY_PLACEHOLDER,
                "preHearingAttendance", HEARING_EMPTY_PLACEHOLDER,
                "hearingTime", HEARING_EMPTY_PLACEHOLDER
            );
        }

        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearingBooking.getVenue());

        return ImmutableMap.of(
            "hearingDate", getHearingDateIfHearingsOnSameDay(hearingBooking).orElse(""),
            "hearingVenue", hearingVenueLookUpService.buildHearingVenue(hearingVenue),
            "preHearingAttendance", extractPrehearingAttendance(hearingBooking),
            "hearingTime", getHearingTime(hearingBooking)
        );
    }

    public Map<String, Object> getJudgeAndLegalAdvisorData(final JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return ImmutableMap.of(
            "judgeTitleAndName", defaultIfBlank(formatJudgeTitleAndName(judgeAndLegalAdvisor), EMPTY_PLACEHOLDER),
            "legalAdvisorName", getLegalAdvisorName(judgeAndLegalAdvisor)
        );
    }

    public String extractPrehearingAttendance(HearingBooking booking) {
        LocalDateTime time = calculatePrehearingAttendance(booking.getStartDate());

        return booking.hasDatesOnSameDay() ? formatTime(time) : formatDateTimeWithYear(time);
    }

    private LocalDateTime calculatePrehearingAttendance(LocalDateTime dateTime) {
        return dateTime.minusHours(1);
    }

    private String formatDateTimeWithYear(LocalDateTime dateTime) {
        return dateFormatterService.formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM yyyy, h:mma");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateFormatterService.formatLocalDateTimeBaseUsingFormat(dateTime, "d MMMM, h:mma");
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateFormatterService.formatLocalDateTimeBaseUsingFormat(dateTime, "h:mma");
    }
}
