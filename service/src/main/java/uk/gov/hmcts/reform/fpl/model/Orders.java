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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@HasEPOAddress
@HasEPOType
@HasEnteredEPOExcluded
@Jacksonized
public class Orders {
    @CCD(label = "Which orders do you need?")
    @NotNull(message = "Select at least one type of order")
    @Size(min = 1, message = "Select at least one type of order")
    private final List<OrderType> orderType;
    @CCD(
            label = "Do you need any of these related orders?",
            showCondition = "orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EmergencyProtectionOrderType"
    )
    private final List<EmergencyProtectionOrdersType> emergencyProtectionOrders;
    @CCD(label = "Do you need any other directions?", typeOverride = FieldType.YesOrNo)
    private final String directions;
    @CCD(
            label = "Do you need any of these directions?",
            showCondition = "orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EmergencyProtectionOrderDirectionType"
    )
    private final List<EmergencyProtectionOrderDirectionsType> emergencyProtectionOrderDirections;
    @CCD(
            label = "Which order do you need?",
            showCondition = "orderType CONTAINS \"OTHER\"",
            typeOverride = FieldType.TextArea
    )
    private final String otherOrder;
    @CCD(
            label = "Give details",
            showCondition = "emergencyProtectionOrders CONTAINS \"OTHER\" AND orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String emergencyProtectionOrderDetails;
    @CCD(
            label = "Give details",
            showCondition = "emergencyProtectionOrderDirections CONTAINS \"OTHER\" AND orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String emergencyProtectionOrderDirectionDetails;
    @CCD(label = "Give details", showCondition = "directions=\"Yes\"", typeOverride = FieldType.TextArea)
    private final String directionDetails;
    @CCD(
            label = "What type of EPO are you requesting?",
            showCondition = "orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\""
    )
    private final EPOType epoType;
    @CCD(
            label = "Who's excluded?",
            showCondition = "emergencyProtectionOrderDirections CONTAINS \"EXCLUSION_REQUIREMENT\" AND orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\""
    )
    private final String excluded;
    @CCD(
            label = "Address",
            showCondition = "epoType=\"PREVENT_REMOVAL\" AND orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
            typeOverride = FieldType.AddressUK
    )
    private final Address address;
    @CCD(
            label = "Under which section are you applying?",
            showCondition = "orderType CONTAINS \"SECURE_ACCOMMODATION_ORDER\""
    )
    @NotNull(message = "Select under which section are you applying", groups = SecureAccommodationGroup.class)
    private final SecureAccommodationOrderSection secureAccommodationOrderSection;
    @CCD(
            label = "Which court are you issuing for?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "Court"
    )
    private final String court;
    @CCD(
            label = "The direction(s) sought in respect of the assessment",
            showCondition = "orderType CONTAINS \"CHILD_ASSESSMENT_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String childAssessmentOrderAssessmentDirections;
    @CCD(
            label = "The direction(s) sought in respect of contact",
            showCondition = "orderType CONTAINS \"CHILD_ASSESSMENT_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String childAssessmentOrderContactDirections;
    @CCD(
            label = "The orders and directions applied for",
            showCondition = "orderType CONTAINS \"CHILD_RECOVERY_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String childRecoveryOrderDirectionsAppliedFor;
    @CCD(
            label = "State whether the child[ren] [is] [are]",
            showCondition = "orderType CONTAINS \"CHILD_RECOVERY_ORDER\""
    )
    private final List<ParticularsOfChildren> particularsOfChildren;
    @CCD(
            label = "Details that will identify the child",
            showCondition = "orderType CONTAINS \"CHILD_RECOVERY_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String particularsOfChildrenDetails;
    @CCD(
            label = "The orders and directions applied for",
            showCondition = "orderType CONTAINS \"EDUCATION_SUPERVISION_ORDER\"",
            typeOverride = FieldType.TextArea
    )
    private final String educationSupervisionOrderDirectionsAppliedFor;
    @CCD(
            label = "State the name of any other local authority which has been consulted",
            showCondition = "orderType CONTAINS \"EDUCATION_SUPERVISION_ORDER\""
    )
    private final String educationSupervisionOrderPriorConsultationOtherLA;
    @CCD(
            label = " ",
            showCondition = "orderType CONTAINS \"EDUCATION_SUPERVISION_ORDER\" AND educationSupervisionOrderPriorConsultationOtherLA != \"\""
    )
    private final List<PriorConsultationType> educationSupervisionOrderPriorConsultationType;

    @JsonIgnore
    public boolean orderContainsEPO() {
        return isNotEmpty(orderType) && this.getOrderType().contains(EMERGENCY_PROTECTION_ORDER);
    }

    @JsonIgnore
    public boolean isC1Order() {
        return isChildAssessmentOrder()
                || isSecureAccommodationOrder()
                || isRefuseContactWithChildApplication()
                || isChildRecoveryOrder()
                || isContactWithChildInCareOrder()
                || isEducationSupervisionOrder();
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
    public boolean isChildAssessmentOrder() {
        return isNotEmpty(orderType) && this.getOrderType().contains(CHILD_ASSESSMENT_ORDER);
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

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## Emergency protection order",
          showCondition = "orderType CONTAINS \"EMERGENCY_PROTECTION_ORDER\"",
          typeOverride = FieldType.Label
  )
  private String emergencyProtectionOrderLabel;
  @CCD(
          label = "## Variation of supervision order or discharge of care order",
          showCondition = "orderType CONTAINS \"OTHER\"",
          typeOverride = FieldType.Label
  )
  private String otherOrderLabel;
  @CCD(
          label = "## Child Assessment Order",
          showCondition = "orderType CONTAINS \"CHILD_ASSESSMENT_ORDER\"",
          typeOverride = FieldType.Label
  )
  private String childAssessmentOrderLabel;
  @CCD(
          label = "## Education Supervision Order",
          showCondition = "orderType CONTAINS \"EDUCATION_SUPERVISION_ORDER\"",
          typeOverride = FieldType.Label
  )
  private String educationSupervisionOrderLabel;
  @CCD(label = "## Directions", showCondition = "orderType = \"DO_NOT_SHOW\"", typeOverride = FieldType.Label)
  private String directionLabel;
  @CCD(
          label = "## Secure accommodation order",
          showCondition = "orderType CONTAINS \"SECURE_ACCOMMODATION_ORDER\"",
          typeOverride = FieldType.Label
  )
  private String secureAccommodationOrderLabel;
  @CCD(
          label = "## Child recovery order",
          showCondition = "orderType CONTAINS \"CHILD_RECOVERY_ORDER\"",
          typeOverride = FieldType.Label
  )
  private String childRecoveryOrderLabel;
  // ==== end synthesised definition-only fields ====
}
