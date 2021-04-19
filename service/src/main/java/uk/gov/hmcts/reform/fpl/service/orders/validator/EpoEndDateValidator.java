package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_ORDER_DETAILS;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EpoEndDateValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Emergency protection orders cannot last longer than 8 days";

    private static final Duration maxEpoEndTime = Duration.of(8, ChronoUnit.DAYS);

    @Override
    public OrderQuestionBlock accept() {
        return EPO_ORDER_DETAILS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final LocalDateTime epoApprovalTime = caseData.getManageOrdersEventData().getManageOrdersApprovalDateTime();
        final LocalDateTime epoEndTime = caseData.getManageOrdersEventData().getManageOrdersEndDateTime();

        if (epoApprovalTime != null && epoEndTime != null
            && (epoEndTime.isBefore(epoApprovalTime) || epoEndTime.isAfter(epoApprovalTime.plus(maxEpoEndTime)))) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
