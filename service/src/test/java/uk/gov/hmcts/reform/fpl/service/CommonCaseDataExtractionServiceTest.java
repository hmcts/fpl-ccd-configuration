package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, HearingVenueLookUpService.class,
    DateFormatterService.class})
class CommonCaseDataExtractionServiceTest {
    @Autowired
    private HearingVenueLookUpService hearingVenueLookUpService;
    private DateFormatterService dateFormatterService = new DateFormatterService();

    private CommonCaseDataExtractionService commonCaseDataExtractionService;
    private HearingBooking hearingBooking;

    @BeforeEach
    void setup() {
        this.commonCaseDataExtractionService = new CommonCaseDataExtractionService(
            dateFormatterService, hearingVenueLookUpService);
    }

    @Test
    void shouldReturnTheFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final Optional<String> hearingDate = commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
            hearingBooking);

        assertThat(hearingDate.isEmpty());
    }

    @Test
    void shouldReturnAnEmptyStringWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final Optional<String> hearingDate = commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
            hearingBooking);

        assertThat(hearingDate.orElse("")).isEqualTo("11 December 2020");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWithDatesWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String hearingTime = commonCaseDataExtractionService.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("11 December, 3:30pm - 12 December, 4:30pm");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String hearingTime = commonCaseDataExtractionService.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("3:30pm - 4:30pm");
    }

    @Test
    void shouldReturnAFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String prehearingAttendance = commonCaseDataExtractionService.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("11 December 2020, 2:30pm");
    }

    @Test
    void shouldReturnAFormattedTimeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String prehearingAttendance = commonCaseDataExtractionService.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("2:30pm");
    }

    private HearingBooking createHearingBookingWithTimesOnSameDay() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 11, 16, 30));
    }

    private HearingBooking createHearingBookingWithTimesOnDifferentDays() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 12, 16, 30));
    }
}
