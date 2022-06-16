package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.GroundsForSecureAccommodationOrder;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.List;
import javax.validation.groups.Default;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class GroundsChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        if (hasEmergencyProtectionOrder(caseData)) {
            return super.validate(caseData, List.of("grounds", "groundsForEPO"), Default.class, EPOGroup.class);
        } else if (hasSecureAccommodationOrder(caseData)) {
            return super.validate(caseData, List.of("groundsForSecureAccommodationOrder"),
                SecureAccommodationGroup.class);
        } else {
            return super.validate(caseData, List.of("grounds"));
        }
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isGroundsStarted(caseData.getGrounds()) || isEPOGroundsStarted(caseData.getGroundsForEPO())
            || isSecureAccommodationOrderGroundsStarted(caseData.getGroundsForSecureAccommodationOrder());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        if (hasSecureAccommodationOrder(caseData)) {
            return isSecureAccommodationOrderGroundsCompleted(caseData.getGroundsForSecureAccommodationOrder());
        }
        return true;
    }

    private boolean hasEmergencyProtectionOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
                && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER);
    }

    private boolean hasSecureAccommodationOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
               && caseData.getOrders().getOrderType().contains(OrderType.SECURE_ACCOMMODATION_ORDER);
    }

    private static boolean isGroundsStarted(Grounds grounds) {
        return isNotEmpty(grounds) && anyNonEmpty(grounds.getThresholdReason(), grounds.getThresholdDetails());
    }

    private static boolean isEPOGroundsStarted(GroundsForEPO grounds) {
        return isNotEmpty(grounds) && isNotEmpty(grounds.getReason());
    }

    private static boolean isSecureAccommodationOrderGroundsStarted(GroundsForSecureAccommodationOrder saoGrounds) {
        return isNotEmpty(saoGrounds)
               && anyNonEmpty(saoGrounds.getGrounds(), saoGrounds.getReasonAndLength(),
                    saoGrounds.getSupportingDocuments());
    }

    private static boolean isSecureAccommodationOrderGroundsCompleted(GroundsForSecureAccommodationOrder saoGrounds) {
        return isNotEmpty(saoGrounds) && isNotEmpty(saoGrounds.getGrounds())
               && isNotEmpty(saoGrounds.getReasonAndLength());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }

}
