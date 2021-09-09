package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.HOURS_MINS;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PastHearingDatesValidatorService {
    private static final LocalTime MIDNIGHT = LocalTime.of(0, 0, 0);

    public List<String> validateHearingDates(LocalDateTime hearingStartDate, LocalDateTime hearingEndDate) {
        List<String> errors = new ArrayList<>();

        if (hearingStartDate != null && isMidnight(hearingStartDate)) {
            errors.add("Enter a valid start time");
        }

        if (hearingEndDate != null && isMidnight(hearingEndDate)) {
            errors.add("Enter a valid end time");
        }

        if (errors.isEmpty() && hearingStartDate != null && hearingEndDate != null
            && !hearingEndDate.isAfter(hearingStartDate)
        ) {
            errors.add("The end date and time must be after the start date and time");
        }

        return errors;
    }

    public List<String> validateDays(String hearingDuration, Integer hearingDays) {
        List<String> errors = new ArrayList<>();
        if (DAYS.getType().equals(hearingDuration) && hearingDays <= 0) {
            errors.add("Enter valid days");
        }
        return errors;
    }

    public List<String> validateHoursMinutes(String hearingDuration, Integer hearingHours, Integer hearingMinutes) {
        List<String> errors = new ArrayList<>();
        if (HOURS_MINS.getType().equals(hearingDuration)
            && ((hearingHours < 0 || hearingMinutes < 0)
                || (hearingHours == 0 && hearingMinutes == 0))) {
            errors.add("Enter valid hours and minutes");
        }
        return errors;
    }

    private boolean isMidnight(LocalDateTime localDateTime) {
        return localDateTime.toLocalTime().equals(MIDNIGHT);
    }
}
