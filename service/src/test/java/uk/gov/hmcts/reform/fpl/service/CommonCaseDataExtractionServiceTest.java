package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CommonCaseDataExtractionService.class})
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, HearingVenueLookUpService.class, LookupTestConfig.class
})
class CommonCaseDataExtractionServiceTest {
    private HearingBooking hearingBooking;

    @Autowired
    private CommonCaseDataExtractionService service;

    @Test
    void shouldReturnTheFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        Optional<String> hearingDate = service.getHearingDateIfHearingsOnSameDay(hearingBooking);

        assertThat(hearingDate).isEmpty();
    }

    @Test
    void shouldReturnAnEmptyStringWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        Optional<String> hearingDate = service.getHearingDateIfHearingsOnSameDay(hearingBooking);

        assertThat(hearingDate.orElse("")).isEqualTo("11 December 2020");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWithDatesWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String hearingTime = service.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("11 December, 3:30pm - 12 December, 4:30pm");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String hearingTime = service.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("3:30pm - 4:30pm");
    }

    @Test
    void shouldReturnAFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String prehearingAttendance = service.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("11 December 2020, 2:30pm");
    }

    @Test
    void shouldReturnAFormattedTimeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String prehearingAttendance = service.extractPrehearingAttendance(hearingBooking);

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
