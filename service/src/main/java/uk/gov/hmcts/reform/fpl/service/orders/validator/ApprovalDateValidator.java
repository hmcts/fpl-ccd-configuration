package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalDateValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Approval date cannot not be in the future";

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return APPROVAL_DATE;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        LocalDate approvalDate = caseData.getManageOrdersEventData().getManageOrdersApprovalDate();
        LocalDate now = time.now().toLocalDate();

        if (approvalDate.isAfter(now)) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
