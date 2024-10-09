package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.ParticularsOfChildren;
import uk.gov.hmcts.reform.fpl.enums.PriorConsultationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderSection;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOAddress;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOType;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEnteredEPOExcluded;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CHILD_ASSESSMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CHILD_RECOVERY_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CONTACT_WITH_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.REFUSE_CONTACT_WITH_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SECURE_ACCOMMODATION_ORDER;

@Data
@Builder(toBuilder = true)
@HasEPOAddress
@HasEPOType
@HasEnteredEPOExcluded
@Jacksonized
public class Orders {
    @NotNull(message = "Select at least one type of order")
    @Size(min = 1, message = "Select at least one type of order")
    private final List<OrderType> orderType;
    private final List<EmergencyProtectionOrdersType> emergencyProtectionOrders;
    private final String directions;
    private final List<EmergencyProtectionOrderDirectionsType> emergencyProtectionOrderDirections;
    private final String otherOrder;
    private final String emergencyProtectionOrderDetails;
    private final String emergencyProtectionOrderDirectionDetails;
    private final String directionDetails;
    private final EPOType epoType;
    private final String excluded;
    private final Address address;
    @NotNull(message = "Select under which section are you applying", groups = SecureAccommodationGroup.class)
    private final SecureAccommodationOrderSection secureAccommodationOrderSection;
    private final String court;
    private final String childAssessmentOrderAssessmentDirections;
    private final String childAssessmentOrderContactDirections;
    private final String childRecoveryOrderDirectionsAppliedFor;
    private final List<ParticularsOfChildren> particularsOfChildren;
    private final String particularsOfChildrenDetails;
    private final String educationSupervisionOrderDirectionsAppliedFor;
    private final String educationSupervisionOrderPriorConsultationOtherLA;
    private final List<PriorConsultationType> educationSupervisionOrderPriorConsultationType;

    @JsonIgnore
    public boolean orderContainsEPO() {
        return isNotEmpty(orderType) && this.getOrderType().contains(EMERGENCY_PROTECTION_ORDER);
    }

    @JsonIgnore
    public boolean isC1Order() {
        return isNotEmpty(orderType) && (this.getOrderType().contains(CHILD_ASSESSMENT_ORDER)
                || isSecureAccommodationOrder()
                || isRefuseContactWithChildApplication()
                || isChildRecoveryOrder()
                || isContactWithChildInCareOrder()
                || isEducationSupervisionOrder());
    }

    @JsonIgnore
    public boolean isDischargeOfCareOrder() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(OTHER);
    }

    @JsonIgnore
    public boolean isSecureAccommodationOrder() {
        return isNotEmpty(orderType) && this.getOrderType().contains(SECURE_ACCOMMODATION_ORDER);
    }

    @JsonIgnore
    public boolean isRefuseContactWithChildApplication() {
        return isNotEmpty(orderType) && this.getOrderType().contains(REFUSE_CONTACT_WITH_CHILD);
    }

    @JsonIgnore
    public boolean isChildRecoveryOrder() {
        return isNotEmpty(orderType) && this.getOrderType().contains(CHILD_RECOVERY_ORDER);
    }

    @JsonIgnore
    public boolean isContactWithChildInCareOrder() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(CONTACT_WITH_CHILD_IN_CARE);
    }

    @JsonIgnore
    public boolean isEducationSupervisionOrder() {
        return isNotEmpty(orderType) && orderType.contains(EDUCATION_SUPERVISION_ORDER);
    }

    @JsonIgnore
    public boolean containsInterimCareOrder() {
        return isNotEmpty(orderType) && orderType.contains(INTERIM_CARE_ORDER);
    }

    @JsonIgnore
    public boolean containsCareOrder() {
        return isNotEmpty(orderType) && orderType.contains(OrderType.CARE_ORDER);
    }

    @JsonIgnore
    public boolean isCareOrderCombinedWithEPOorICO() {
        return isNotEmpty(orderType) && (containsCareOrder() && (containsInterimCareOrder() || orderContainsEPO()));
    }

    @JsonIgnore
    public boolean isEmergencyProtectionOrderOnly() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(EMERGENCY_PROTECTION_ORDER);
    }

    @JsonIgnore
    public boolean isInterimCareOrderOnly() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(INTERIM_CARE_ORDER);
    }

    @JsonIgnore
    public boolean isSecureAccommodationOrderOnly() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(SECURE_ACCOMMODATION_ORDER);
    }

    @JsonIgnore
    public boolean isChildRecoveryOrderOnly() {
        return isNotEmpty(orderType) && orderType.size() == 1 && orderType.contains(CHILD_RECOVERY_ORDER);
    }

    @JsonIgnore
    public boolean isEPOCombinedWithICO() {
        return isNotEmpty(orderType) && containsInterimCareOrder() && orderContainsEPO();
    }
}
