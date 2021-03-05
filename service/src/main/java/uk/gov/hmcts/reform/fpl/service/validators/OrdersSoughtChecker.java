package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
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
            }
            if (isEmergencyProtectionOtherNotCompleted(orders)) {
                return false;
            }
            if (isEmergencyProtectionOrderOtherDirectionNotCompleted(orders)) {
                return false;
            }
            if (isEmergencyProtectionOrderDirectionExclusionNotCompleted(orders)) {
                return false;
            }
        }

        if (orders.getOrderType().contains(OrderType.OTHER)
            && isEmpty(orders.getOtherOrder())) {
            return false;
        }

        return !YES.getValue().equals(orders.getDirections())
            || !isEmpty(orders.getDirectionDetails());
    }

    private boolean isEmergencyProtectionOrderDirectionExclusionNotCompleted(Orders orders) {
        return orders.getEmergencyProtectionOrderDirections() != null
            && orders.getEmergencyProtectionOrderDirections().contains(EXCLUSION_REQUIREMENT)
            && isEmpty(orders.getExcluded());
    }

    private boolean isEmergencyProtectionOrderOtherDirectionNotCompleted(Orders orders) {
        return orders.getEmergencyProtectionOrderDirections() != null
            && orders.getEmergencyProtectionOrderDirections().contains(OTHER)
            && isEmpty(orders.getEmergencyProtectionOrderDirectionDetails());
    }

    private boolean isEmergencyProtectionOtherNotCompleted(Orders orders) {
        return orders.getEmergencyProtectionOrders() != null
            && orders.getEmergencyProtectionOrders().contains(EmergencyProtectionOrdersType.OTHER)
            && isEmpty(orders.getEmergencyProtectionOrderDetails());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
