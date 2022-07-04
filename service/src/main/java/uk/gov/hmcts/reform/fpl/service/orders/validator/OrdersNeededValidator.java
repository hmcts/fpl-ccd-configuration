package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdersNeededValidator {

    public List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        List<OrderType> orderTypes = caseData.getOrders().getOrderType();

        if (orderTypes != null && orderTypes.size() > 1 && caseData.isC1Application()) {
            errors.add("You have selected a standalone order, this cannot be applied for alongside other orders.");
        }

        return errors;
    }
}
