package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class OrdersSoughtChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("orders"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final Orders orders = caseData.getOrders();

        if (isEmpty(orders)) {
            return false;
        }

        return anyNonEmpty(orders.getOrderType(), orders.getDirections());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Orders orders = caseData.getOrders();

        if (orders == null || anyEmpty(orders.getOrderType(), orders.getDirections())) {
            return false;
        } else {
            return ("No").equals(orders.getDirections())
                || !isEmpty(orders.getDirectionDetails());
        }
    }
}
