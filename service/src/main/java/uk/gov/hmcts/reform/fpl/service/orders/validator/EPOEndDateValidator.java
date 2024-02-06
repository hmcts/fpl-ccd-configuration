package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalTime.MIDNIGHT;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EPOEndDateValidator implements QuestionBlockOrderValidator {

    private static final String INVALID_TIME_MESSAGE = "Enter a valid time";
    private static final String UP_TO_2_YEARS_BEHIND_MESSAGE = "Enter an end date up to 2 years behind";
    public static final String BEFORE_APPROVAL_MESSAGE = "Enter a date after the approval date";
    public static final String END_DATE_RANGE_MESSAGE = "Emergency protection orders cannot last longer than 1 year";
    private static final Duration EPO_END_DATE_RANGE = Duration.of(365, ChronoUnit.DAYS);

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return EPO_EXPIRY_DATE;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final LocalDateTime epoApprovalTime = caseData.getManageOrdersEventData().getManageOrdersApprovalDateTime();
        final LocalDateTime epoEndTime = caseData.getManageOrdersEventData().getManageOrdersEndDateTime();

        List<String> errors = validateEpoEndDateTime(epoEndTime);

        final LocalDateTime rangeEnd = epoApprovalTime.plus(EPO_END_DATE_RANGE);
        if (errors.isEmpty() && epoEndTime.isAfter(rangeEnd)) {
            return List.of(END_DATE_RANGE_MESSAGE);
        }

        if (errors.isEmpty() && epoEndTime.isBefore(epoApprovalTime)) {
            return List.of(BEFORE_APPROVAL_MESSAGE);
        }

        return errors;
    }

    private List<String> validateEpoEndDateTime(LocalDateTime epoEndTime) {
        List<String> errors = new ArrayList<>();
        long diff = ChronoUnit.MILLIS.between(time.now(), epoEndTime);

        if (diff > Duration.ofDays(365).multipliedBy(2).toMillis()) {
            errors.add(UP_TO_2_YEARS_BEHIND_MESSAGE);
        }

        if (epoEndTime.toLocalTime().equals(MIDNIGHT)) {
            errors.add(INVALID_TIME_MESSAGE);
        }

        return errors;
    }
}
