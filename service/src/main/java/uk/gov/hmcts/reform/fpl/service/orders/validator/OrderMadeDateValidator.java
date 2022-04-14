package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_PLACED_CHILD_IN_CUSTODY;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderMadeDateValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "The date of the order cannot not be in the future";

    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return ORDER_PLACED_CHILD_IN_CUSTODY;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        LocalDate orderMade = caseData.getManageOrdersEventData().getManageOrdersOrderCreatedDate();
        LocalDate now = time.now().toLocalDate();

        List<String> errors = new ArrayList<>();

        if (orderMade.isAfter(now)) {
            errors.add(MESSAGE);
        }

        return errors;
    }
}
