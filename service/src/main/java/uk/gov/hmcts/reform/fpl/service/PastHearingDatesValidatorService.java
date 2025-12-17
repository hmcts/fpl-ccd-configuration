package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
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

    public List<String> validateVacatedDate(LocalDateTime hearingEndDate, LocalDate vacatedDate) {
        List<String> errors = new ArrayList<>();

        if (vacatedDate != null && vacatedDate.isAfter(hearingEndDate.toLocalDate())) {
            errors.add("The vacated date must be before, or the same as the hearing date.");
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

    private boolean isInvalidField(Object s) {
        if (s == null) {
            return false;
        }
        try {
            Integer.parseInt(s.toString());
            return false;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    public List<String> validateHearingIntegers(CaseDetails caseDetails) {
        List<String> errors = new ArrayList<>();
        if (isInvalidField(caseDetails.getData().get("hearingHours"))) {
            errors.add("Hearing length, in hours should be a whole number");
        }
        if (isInvalidField(caseDetails.getData().get("hearingMinutes"))
            || Integer.parseInt(caseDetails.getData().get("hearingMinutes").toString()) >= 60) {
            errors.add("Hearing length, in minutes should be a whole number between 0 and 59");
        }
        if (isInvalidField(caseDetails.getData().get("hearingDays"))) {
            errors.add("Hearing length, in days should be a whole number");
        }
        return errors;
    }
}
