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
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalDateTimeValidator implements QuestionBlockOrderValidator {
    public static final String APPROVAL_DATE_RANGE_MESSAGE = "Approval date cannot be more than 1 year in the future";
    private static final Duration APPROVAL_END_DATE_RANGE = Duration.of(365, ChronoUnit.DAYS);
    private final Time time;
    @Override
    public OrderQuestionBlock accept() {
        return APPROVAL_DATE_TIME;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        LocalDateTime approvalDateTime = caseData.getManageOrdersEventData().getManageOrdersApprovalDateTime();

        final LocalDateTime rangeEnd = time.now().plus(APPROVAL_END_DATE_RANGE);
        if (approvalDateTime.isAfter(rangeEnd)) {
            return List.of(APPROVAL_DATE_RANGE_MESSAGE);
        }

        return List.of();
    }
}
