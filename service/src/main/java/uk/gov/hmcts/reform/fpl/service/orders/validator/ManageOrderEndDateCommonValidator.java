package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderEndDateCommonValidator {
    private static final Integer MINIMUM_MONTHS_VALID = 1;
    private static final Integer MAXIMUM_MONTHS_VALID = 12;
    private static final String AFTER_APPROVAL_DATE_MESSAGE = "Enter an end date after the approval date";
    private static final String END_DATE_MIN_RANGE_MESSAGE = "This order must last for at least 1 month";
    private static final String END_DATE_MAX_RANGE_MESSAGE = "This order cannot last longer than 12 months";
    private static final List<String> NO_VALIDATION = List.of();

    private final Time time;

    public List<String> validate(
        ManageOrdersEndDateType type,
        LocalDate endDate,
        LocalDateTime endDateTime,
        LocalDate approvalDate,
        Integer expireInNumberOfMonths) {

        switch (type) {
            case CALENDAR_DAY:
                return validateDate(approvalDate, endDate);
            case CALENDAR_DAY_AND_TIME:
                return validateDateTime(approvalDate, endDateTime);
            case NUMBER_OF_MONTHS:
                return validateMonth(expireInNumberOfMonths);
            case END_OF_PROCEEDINGS:
                return NO_VALIDATION;
            default:
                throw new IllegalStateException("Unexpected order end date type: " + type);
        }
    }

    private List<String> validateMonth(int numberOfMonths) {
        List<String> errors = new ArrayList<>();

        if (numberOfMonths < MINIMUM_MONTHS_VALID) {
            errors.add(END_DATE_MIN_RANGE_MESSAGE);
        }

        if (numberOfMonths > MAXIMUM_MONTHS_VALID) {
            errors.add(END_DATE_MAX_RANGE_MESSAGE);
        }

        return errors;
    }

    private List<String> validateDate(LocalDate approvalDate, LocalDate endDate) {
        List<String> errors = new ArrayList<>();

        if (!endDate.isAfter(approvalDate)) {
            errors.add(AFTER_APPROVAL_DATE_MESSAGE);
        }

        if (endDate.isAfter(approvalDate.plusMonths(MAXIMUM_MONTHS_VALID))) {
            errors.add(END_DATE_MAX_RANGE_MESSAGE);
        }

        return errors;
    }


    private List<String> validateDateTime(LocalDate approvalDate, LocalDateTime endDate) {
        List<String> errors = new ArrayList<>();
        LocalDateTime approvalDateTime = approvalDate.atStartOfDay();

        if (!endDate.isAfter(approvalDateTime)) {
            errors.add(AFTER_APPROVAL_DATE_MESSAGE);
        }

        if (endDate.isAfter(approvalDateTime.plusMonths(MAXIMUM_MONTHS_VALID))) {
            errors.add(END_DATE_MAX_RANGE_MESSAGE);
        }

        return errors;
    }

}
