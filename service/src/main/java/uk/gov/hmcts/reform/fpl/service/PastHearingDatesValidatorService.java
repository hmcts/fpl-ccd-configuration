package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        if (hearingStartDate != null && !isValid(hearingStartDate)) {
            errors.add("Enter a valid start time");
        }

        if (hearingEndDate != null &!isValid(hearingEndDate)) {
                errors.add("Enter a valid end time");
        }

        return errors;
    }

    public boolean isValid(LocalDateTime localDateTime) {
        return !localDateTime.toLocalTime().equals(MIDNIGHT);
    }


}
