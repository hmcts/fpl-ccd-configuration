package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
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
        }

        if (orders.orderContainsEPO()) {
            if (isEmpty(orders.getEpoType())) {
                return false;
            } else if (orders.getEmergencyProtectionOrders() != null
                && orders.getEmergencyProtectionOrders().contains(EmergencyProtectionOrdersType.OTHER)
                && isEmpty(orders.getEmergencyProtectionOrderDirectionDetails())) {
                return false;
            } else if (orders.getEmergencyProtectionOrderDirections() != null
                && orders.getEmergencyProtectionOrderDirections().contains(EXCLUSION_REQUIREMENT)) {
                if (isEmpty(orders.getExcluded())) {
                    return false;
                }
            }
        }

        if (orders.getOrderType().contains(OrderType.OTHER) && isEmpty(orders.getOtherOrder())) {
            return false;
        } else if (("Yes").equals(orders.getDirections())
            && isEmpty(orders.getDirectionDetails())) {
            return false;
        }

        return super.isCompleted(caseData);
    }
}
