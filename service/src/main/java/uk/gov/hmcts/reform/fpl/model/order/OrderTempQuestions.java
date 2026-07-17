package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
public class OrderTempQuestions {
    @CCD(label = " ", showCondition = "hearingDetails=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String hearingDetails;
    @CCD(label = " ", showCondition = "linkApplication=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String linkApplication;
    @CCD(label = " ", showCondition = "approver=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String approver;
    @CCD(label = " ", showCondition = "approvalDate=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String approvalDate;
    @CCD(label = " ", showCondition = "approvalDateTime=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String approvalDateTime;
    @CCD(label = " ", showCondition = "cafcassJurisdictions=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String cafcassJurisdictions;
    @CCD(label = " ", showCondition = "whichChildren=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String whichChildren;
    @CCD(label = " ", showCondition = "orderTitle=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderTitle;
    @CCD(label = " ", showCondition = "selectSingleChild=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String selectSingleChild;
    @CCD(label = " ", showCondition = "reasonForSecureAccommodation=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String reasonForSecureAccommodation;
    @CCD(label = " ", showCondition = "childLegalRepresentation=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String childLegalRepresentation;
    @CCD(label = " ", showCondition = "orderJurisdiction=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderJurisdiction;
    @CCD(label = " ", showCondition = "dischargeOfCareDetails=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String dischargeOfCareDetails;
    @CCD(
            label = " ",
            showCondition = "childArrangementSpecificIssueProhibitedSteps=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String childArrangementSpecificIssueProhibitedSteps;
    @CCD(label = " ", showCondition = "epoTypeAndPreventRemoval=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String epoTypeAndPreventRemoval;
    @CCD(label = " ", showCondition = "epoIncludePhrase=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String epoIncludePhrase;
    @CCD(label = " ", showCondition = "epoChildrenDescription=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String epoChildrenDescription;
    @CCD(label = " ", showCondition = "epoExpiryDate=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String epoExpiryDate;
    @CCD(label = " ", showCondition = "furtherDirections=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String furtherDirections;
    @CCD(label = " ", showCondition = "orderDetails=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderDetails;
    @CCD(label = " ", showCondition = "previewOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String isFinalOrder;
    @CCD(label = " ", showCondition = "translationRequirements=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String translationRequirements;
    @CCD(
            label = " ",
            showCondition = "manageOrdersExpiryDateWithMonth=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String manageOrdersExpiryDateWithMonth;
    @CCD(
            label = " ",
            showCondition = "manageOrdersExpiryDateWithEndOfProceedings=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String manageOrdersExpiryDateWithEndOfProceedings;
    @CCD(
            label = " ",
            showCondition = "manageOrdersExclusionRequirementDetails=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String manageOrdersExclusionRequirementDetails;
    @CCD(label = " ", showCondition = "needSealing=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String needSealing;
    @CCD(label = " ", showCondition = "previewOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String closeCase;
    @CCD(label = " ", showCondition = "uploadOrderFile=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String uploadOrderFile;
    @CCD(label = " ", showCondition = "previewOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String previewOrder;
    @CCD(label = " ", showCondition = "appointedGuardian=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String appointedGuardian;
    @CCD(label = " ", showCondition = "respondentsRefused=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String respondentsRefused;
    @CCD(label = " ", showCondition = "refuseContactQuestions=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String refuseContactQuestions;
    @CCD(label = " ", showCondition = "orderIsByConsent=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderIsByConsent;
    @CCD(label = " ", showCondition = "whichOthers=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String whichOthers;
    @CCD(label = " ", showCondition = "orderToAmend=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderToAmend;
    @CCD(label = " ", showCondition = "uploadAmendedOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String uploadAmendedOrder;
    @CCD(label = " ", showCondition = "parentResponsible=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String parentResponsible;
    @CCD(label = " ", showCondition = "orderPlacedChildInCustody=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String orderPlacedChildInCustody;
    @CCD(label = " ", showCondition = "childPlacementApplications=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String childPlacementApplications;
    @CCD(label = " ", showCondition = "childPlacementQuestions=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String childPlacementQuestions;
    @CCD(
            label = " ",
            showCondition = "childPlacementQuestionsForBlankOrder=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String childPlacementQuestionsForBlankOrder;
    @CCD(label = " ", showCondition = "manageOrdersChildAssessment=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String manageOrdersChildAssessment;
    @CCD(
            label = " ",
            showCondition = "manageOrdersEducationSupervision=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String manageOrdersEducationSupervision;
    @CCD(
            label = " ",
            showCondition = "manageOrdersVaryOrExtendSupervisionOrder=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String manageOrdersVaryOrExtendSupervisionOrder;
    @CCD(label = " ", showCondition = "leaveToChangeChildSurname=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String leaveToChangeChildSurname;
    @CCD(
            label = " ",
            showCondition = "partyAllowedContactsAndConditions=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    String partyAllowedContactsAndConditions;
    @CCD(label = " ", showCondition = "declarationOfParentage=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String declarationOfParentage;
    @CCD(label = " ", showCondition = "familyAssistanceOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String familyAssistanceOrder;
    @CCD(label = " ", showCondition = "nonMolestationOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String nonMolestationOrder;
    @CCD(label = " ", showCondition = "manageOrdersTransparencyOrder=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    String manageOrdersTransparencyOrder;
}
