package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithEndOfProceedings;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderEndDateWithEndOfProceedingsValidator implements QuestionBlockOrderValidator {
    private static final Integer MAXIMUM_MONTHS_VALID = 12;
    private static final String AFTER_APPROVAL_DATE_MESSAGE = "Enter an end date after the approval date";
    private static final String END_DATE_MAX_RANGE_MESSAGE = "This order cannot last longer than 12 months";

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        final ManageOrdersEndDateTypeWithEndOfProceedings type =
            manageOrdersEventData.getManageOrdersEndDateTypeWithEndOfProceedings();
        final LocalDate endDate = manageOrdersEventData.getManageOrdersSetDateEndDate();
        final LocalDateTime endDateTime = manageOrdersEventData.getManageOrdersSetDateAndTimeEndDate();
        final LocalDate approvalDate = manageOrdersEventData.getManageOrdersApprovalDate();

        switch (type) {
            case SET_CALENDAR_DAY:
                return validateDate(approvalDate, endDate);
            case SET_CALENDAR_DAY_AND_TIME:
                return validateDateTime(approvalDate, endDateTime);
            case SET_END_OF_PROCEEDINGS:
                return validateEndOfProceedings();
            default:
                throw new IllegalStateException("Unexpected order end date type: " + type);
        }
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

    private List<String> validateEndOfProceedings() {
        List<String> errors = new ArrayList<>();

        log.info("No validation needed for SET_END_OF_PROCEEDINGS");

        return errors;
    }
}
