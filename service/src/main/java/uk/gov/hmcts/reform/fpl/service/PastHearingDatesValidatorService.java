package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PastHearingDatesValidatorService {
    private static final LocalTime MIDNIGHT = LocalTime.of(0, 0, 0);

    public List<String> validateHearingDates(LocalDateTime hearingStartDate, LocalDateTime hearingEndDate,
                                             HearingBooking vacatedHearing) {

        List<String> errors = validateHearingDates(hearingStartDate, hearingEndDate);

        if (hearingStartDate.isBefore(LocalDateTime.now())) {
            errors.add("Enter a start date in the future");
        }

        if (hearingEndDate.isBefore(LocalDateTime.now())) {
            errors.add("Enter an end date in the future");
        }

        if (vacatedHearing != null && hearingEndDate.isBefore(vacatedHearing.getEndDate())) {
            errors.add("The end date must be after the vacated hearing");
        }

        return errors;
    }

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

    private boolean isMidnight(LocalDateTime localDateTime) {
        return localDateTime.toLocalTime().equals(MIDNIGHT);
    }
}
