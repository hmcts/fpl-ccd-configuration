package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;

import java.util.List;
import javax.validation.groups.Default;

@Component
public class GroundsValidator extends PropertiesValidator {

    @Override
    public List<String> validate(CaseData caseData) {
        if (hasEmergencyProtectionOrder(caseData)) {
            return super.validate(caseData, List.of("grounds", "groundsForEPO"), Default.class, EPOGroup.class);
        } else {
            return super.validate(caseData, List.of("grounds"));
        }
    }

    private boolean hasEmergencyProtectionOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
                && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER);
    }
}
