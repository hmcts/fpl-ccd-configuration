package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=PastHearingDatesValidatorService.class)
class PastHearingDatesValidatorServiceTest {
    @Autowired
    private PastHearingDatesValidatorService service;

    @Test
    void shouldReturnEmptyValidationErrorsWhenNoHearingDate() {
        final List<String> validationErrors = service.validateHearingDates(null, null);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnEmptyValidationErrorsWhenHearingDateTimeIsValid() {
        LocalDateTime hearingStartDate = LocalDateTime.now();
        LocalDateTime hearingEndDate = LocalDateTime.now();
        final List<String> validationErrors = service.validateHearingDates(hearingStartDate, hearingEndDate);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsWhenHearingDateTimeIsNotValid() {
        LocalDateTime hearingStartDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime hearingEndDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        final List<String> validationErrors = service.validateHearingDates(hearingStartDate, hearingEndDate);

        assertThat(validationErrors).containsExactlyInAnyOrder("Enter a valid start time", "Enter a valid end time");
    }
}
