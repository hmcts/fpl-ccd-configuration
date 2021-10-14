package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> validateVacatedDate(LocalDateTime hearingEndDate, LocalDate vacatedDate) {
        List<String> errors = new ArrayList<>();

        if (vacatedDate != null && vacatedDate.isAfter(hearingEndDate.toLocalDate())) {
            errors.add("The vacated date must be before, or the same as the hearing date.");
        }

        return errors;
    }

    private boolean isMidnight(LocalDateTime localDateTime) {
        return localDateTime.toLocalTime().equals(MIDNIGHT);
    }
}
