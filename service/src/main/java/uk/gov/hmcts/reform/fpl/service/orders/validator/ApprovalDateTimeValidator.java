package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalDateTimeValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Approval date cannot not be in the future";

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return APPROVAL_DATE_TIME;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        LocalDateTime approvalDateTime = caseData.getManageOrdersEventData().getManageOrdersApprovalDateTime();
        LocalDateTime now = time.now();

        if (approvalDateTime.isAfter(now)) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
