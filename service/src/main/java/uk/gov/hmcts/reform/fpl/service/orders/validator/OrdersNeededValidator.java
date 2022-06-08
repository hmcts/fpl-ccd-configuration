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
        // If secure accommodation order is selected, this should be the only order selected
        if(orderTypes != null && orderTypes.contains(OrderType.SECURE_ACCOMMODATION_ORDER) && orderTypes.size() > 1) {
            errors.add("If secure accommodation order is selected, this should be the only order selected");
        }

        return errors;
    }
}
