package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.orders.SupervisionOrderEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SUPERVISION_ORDER_END_DATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SupervisionOrderEndDateValidator implements QuestionBlockOrderValidator {
    private static final Integer MINIMUM_MONTHS_VALID = 1;
    private static final Integer MAXIMUM_MONTHS_VALID = 12;
    private static final String AFTER_APPROVAL_DATE_MESSAGE = "Enter an end date after the approval date";
    private static final String END_DATE_MAX_RANGE_MESSAGE = "This order cannot last longer than 12 months";
    private static final String END_DATE_MIN_RANGE_MESSAGE = "This order must last for at least 1 month";

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return SUPERVISION_ORDER_END_DATE;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        final SupervisionOrderEndDateType type = manageOrdersEventData.getManageSupervisionOrderEndDateType();
        final LocalDate endDate = manageOrdersEventData.getManageOrdersSetDateEndDate();
        final LocalDateTime endDateTime = manageOrdersEventData.getManageOrdersSetDateAndTimeEndDate();
        final Integer expireInNumberOfMonths = manageOrdersEventData.getManageOrdersSetMonthsEndDate();
        final LocalDate approvalDate = manageOrdersEventData.getManageOrdersApprovalDate();

        switch (type) {
            case SET_CALENDAR_DAY:
                return validateDate(approvalDate, endDate);
            case SET_CALENDAR_DAY_AND_TIME:
                return validateDateTime(approvalDate, endDateTime);
            case SET_NUMBER_OF_MONTHS:
                return validateMonth(expireInNumberOfMonths);
            default:
                throw new IllegalStateException("Unexpected supervision order end date type: " + type);
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
