package uk.gov.hmcts.reform.fpl.service.validators;

import jakarta.validation.groups.Default;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForChildAssessmentOrder;
import uk.gov.hmcts.reform.fpl.model.GroundsForContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.GroundsForRefuseContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForSecureAccommodationOrder;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class GroundsChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        if (hasEmergencyProtectionOrder(caseData)) {
            return super.validate(caseData, List.of("grounds", "groundsForEPO"), Default.class, EPOGroup.class);
        } else if (isNotEmpty(caseData.getOrders()) && caseData.getOrders().getOrderType() != null
            && caseData.getOrders().getOrderType().contains(OrderType.CHILD_ASSESSMENT_ORDER)) {
            return super.validate(caseData, List.of("groundsForChildAssessmentOrder"));
        } else if (hasSecureAccommodationOrder(caseData)) {
            return super.validate(caseData, List.of("groundsForSecureAccommodationOrder"),
                SecureAccommodationGroup.class);
        } else if (caseData.isRefuseContactWithChildApplication()) {
            return super.validate(caseData, List.of("groundsForRefuseContactWithChild"));
        } else if (hasChildRecoveryOrder(caseData)) {
            return super.validate(caseData, List.of("groundsForChildRecoveryOrder"));
        } else if (caseData.isContactWithChildInCareApplication()) {
            return super.validate(caseData, List.of("groundsForContactWithChild"));
        } else if (caseData.isEducationSupervisionApplication()) {
            return super.validate(caseData, List.of("groundsForEducationSupervisionOrder"));
        } else {
            return super.validate(caseData, List.of("grounds"));
        }
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isGroundsStarted(caseData.getGrounds())
            || isEPOGroundsStarted(caseData.getGroundsForEPO())
            || isChildAssessmentOrderGroundsStarted(caseData.getGroundsForChildAssessmentOrder())
            || isSecureAccommodationOrderGroundsStarted(caseData.getGroundsForSecureAccommodationOrder())
            || isRefuseContactWithChildGroundsStarted(caseData.getGroundsForRefuseContactWithChild())
            || isContactWithChildGroundsStarted(caseData.getGroundsForContactWithChild());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        if (hasSecureAccommodationOrder(caseData)) {
            return isSecureAccommodationOrderGroundsCompleted(caseData.getGroundsForSecureAccommodationOrder());
        } else if (caseData.isRefuseContactWithChildApplication()) {
            return isRefuseContactWithChildGroundsCompleted(caseData.getGroundsForRefuseContactWithChild());
        } else if (caseData.isContactWithChildInCareApplication()) {
            return isContactWithChildGroundsCompleted(caseData.getGroundsForContactWithChild());
        }
        return super.isCompleted(caseData);
    }

    private boolean hasEmergencyProtectionOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
                && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER);
    }

    private boolean hasSecureAccommodationOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
               && caseData.getOrders().getOrderType().contains(OrderType.SECURE_ACCOMMODATION_ORDER);
    }

    private boolean hasChildRecoveryOrder(CaseData caseData) {
        return caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
               && caseData.getOrders().getOrderType().contains(OrderType.CHILD_RECOVERY_ORDER);
    }

    private static boolean isGroundsStarted(Grounds grounds) {
        return isNotEmpty(grounds) && anyNonEmpty(grounds.getThresholdReason(), grounds.getThresholdDetails());
    }

    private static boolean isEPOGroundsStarted(GroundsForEPO grounds) {
        return isNotEmpty(grounds) && isNotEmpty(grounds.getReason());
    }

    private static boolean isSecureAccommodationOrderGroundsStarted(GroundsForSecureAccommodationOrder saoGrounds) {
        return isNotEmpty(saoGrounds)
               && anyNonEmpty(saoGrounds.getGrounds(), saoGrounds.getReasonAndLength());
    }

    private static boolean isSecureAccommodationOrderGroundsCompleted(GroundsForSecureAccommodationOrder saoGrounds) {
        return isNotEmpty(saoGrounds) && isNotEmpty(saoGrounds.getGrounds())
               && isNotEmpty(saoGrounds.getReasonAndLength());
    }

    private static boolean isChildAssessmentOrderGroundsStarted(GroundsForChildAssessmentOrder grounds) {
        return isNotEmpty(grounds) && isNotEmpty(grounds.getThresholdDetails());
    }

    private static boolean isRefuseContactWithChildGroundsStarted(GroundsForRefuseContactWithChild grounds) {
        return isNotEmpty(grounds)
               && (isNotEmpty(grounds.getLaHasRefusedContact())
                   || isNotEmpty(grounds.getPersonsBeingRefusedContactWithChild())
                   || isNotEmpty(grounds.getPersonHasContactAndCurrentArrangement())
                   || isNotEmpty(grounds.getReasonsOfApplication()));
    }

    private static boolean isRefuseContactWithChildGroundsCompleted(GroundsForRefuseContactWithChild grounds) {
        return isNotEmpty(grounds) && isNotEmpty(grounds.getLaHasRefusedContact())
               && isNotEmpty(grounds.getPersonsBeingRefusedContactWithChild())
               && isNotEmpty(grounds.getPersonHasContactAndCurrentArrangement())
               && isNotEmpty(grounds.getReasonsOfApplication());
    }

    private static boolean isContactWithChildGroundsStarted(GroundsForContactWithChild grounds) {
        return isNotEmpty(grounds)
            && (isNotEmpty(grounds.getParentOrGuardian())
                || isNotEmpty(grounds.getResidenceOrder())
                || isNotEmpty(grounds.getHadCareOfChildrenBeforeCareOrder())
                || isNotEmpty(grounds.getReasonsForApplication()));
    }

    private static boolean isContactWithChildGroundsCompleted(GroundsForContactWithChild grounds) {
        return isNotEmpty(grounds)
            && (isNotEmpty(grounds.getParentOrGuardian())
                && isNotEmpty(grounds.getResidenceOrder())
                && isNotEmpty(grounds.getHadCareOfChildrenBeforeCareOrder())
                && isNotEmpty(grounds.getReasonsForApplication()));
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }

}
