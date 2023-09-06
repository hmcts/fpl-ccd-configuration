package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrderDocumentScopedFieldsCalculatorTest {

    private final ManageOrderDocumentScopedFieldsCalculator underTest = new ManageOrderDocumentScopedFieldsCalculator();

    @Test
    void calculate() {
        assertThat(underTest.calculate()).containsExactlyInAnyOrder(
            "manageOrdersPartyToBeBefriended1",
            "manageOrdersPartyToBeBefriended2",
            "manageOrdersPartyToBeBefriended3",
            "manageOrdersFamilyAssistanceEndDate",
            "manageOrdersAllowedContact1",
            "manageOrdersAllowedContact2",
            "manageOrdersAllowedContact3",
            "manageOrdersConditionsOfContact",
            "manageOrdersApprovedAtHearing",
            "manageOrdersApprovedAtHearingList",
            "manageOrdersUploadType",
            "manageOrdersUploadTypeOtherTitle",
            "manageOrdersUploadOrderFile",
            "manageOrdersNeedSealing",
            "judgeAndLegalAdvisor",
            "manageOrdersApprovalDate",
            "manageOrdersShouldLinkApplication",
            "manageOrdersLinkedApplication",
            "orderAppliesToAllChildren",
            "children_label",
            "childSelector",
            "appointedGuardians_label",
            "appointedGuardianSelector",
            "additionalAppointedSpecialGuardians",
            "manageOrdersIsByConsent",
            "manageOrdersTitle",
            "manageOrdersDirections",
            "manageOrdersCareOrderIssuedDate",
            "manageOrdersCareOrderIssuedCourt",
            "manageOrdersEpoType",
            "manageOrdersIncludePhrase",
            "manageOrdersChildrenDescription",
            "manageOrdersEndDateTime",
            "manageOrdersEpoRemovalAddress",
            "manageOrdersExclusionRequirement",
            "manageOrdersWhoIsExcluded",
            "manageOrdersExclusionStartDate",
            "manageOrdersPowerOfArrest",
            "manageOrdersFurtherDirections",
            "manageOrdersEndDateTypeWithMonth",
            "manageOrdersEndDateTypeWithEndOfProceedings",
            "manageOrdersSetDateEndDate",
            "manageOrdersSetDateAndTimeEndDate",
            "manageOrdersSetMonthsEndDate",
            "manageOrdersMultiSelectListForC43",
            "manageOrdersRecitalsAndPreambles",
            "orderPreview",
            "manageOrdersIsFinalOrder",
            "manageOrdersCloseCase",
            "manageOrdersCloseCaseWarning",
            "manageOrdersApprovalDateTime",
            "manageOrdersOperation",
            "manageOrdersOperationClosedState",
            "manageOrdersType",
            "manageOrdersState",
            "orderTempQuestions",
            "hearingDetailsSectionSubHeader",
            "issuingDetailsSectionSubHeader",
            "childrenDetailsSectionSubHeader",
            "orderDetailsSectionSubHeader",
            "manageOrdersExclusionDetails",
            "manageOrdersHasExclusionRequirement",
            "manageOrdersCafcassRegion",
            "manageOrdersCafcassOfficesEngland",
            "manageOrdersCafcassOfficesWales",
            "othersSelector",
            "others_label",
            "sendOrderToAllOthers",
            "manageOrdersParentResponsible",
            "manageOrdersIsChildRepresented",
            "manageOrdersReasonForSecureAccommodation",
            "manageOrdersOrderJurisdiction",
            "whichChildIsTheOrderFor",
            "manageOrdersAmendmentList",
            "manageOrdersOrderToAmend",
            "manageOrdersAmendedOrder",
            "manageOrdersTranslationNeeded",
            "respondentsRefused_label",
            "respondentsRefusedSelector",
            "manageOrdersRelationshipWithChild",
            "manageOrdersChildPlacementApplication",
            "manageOrdersSerialNumber",
            "manageOrdersBirthCertificateNumber",
            "manageOrdersBirthCertificateDate",
            "manageOrdersBirthCertificateRegistrationDistrict",
            "manageOrdersBirthCertificateRegistrationSubDistrict",
            "manageOrdersBirthCertificateRegistrationCounty",
            "manageOrdersPlacementOrderOtherDetails",
            "manageOrdersLeaName",
            "manageOrdersEndDateWithEducationAge",
            "manageOrdersPlacedUnderOrder",
            "manageOrdersIsExParte",
            "manageOrdersActionsPermitted",
            "manageOrdersChildAssessmentType",
            "manageOrdersAssessmentStartDate",
            "manageOrdersDurationOfAssessment",
            "manageOrdersPlaceOfAssessment",
            "manageOrdersAssessingBody",
            "manageOrdersChildKeepAwayFromHome",
            "manageOrdersFullAddressToStayIfKeepAwayFromHome",
            "manageOrdersStartDateOfStayIfKeepAwayFromHome",
            "manageOrdersEndDateOfStayIfKeepAwayFromHome",
            "manageOrdersChildFirstContactIfKeepAwayFromHome",
            "manageOrdersChildSecondContactIfKeepAwayFromHome",
            "manageOrdersChildThirdContactIfKeepAwayFromHome",
            "manageOrdersDoesCostOrderExist",
            "manageOrdersCostOrderDetails",
            "manageOrdersSupervisionOrderType",
            "manageOrdersC35aOrderExists",
            "manageOrdersC35aOrderDoesntExistMessage",
            "manageOrdersSupervisionOrderVariationHeading",
            "manageOrdersSupervisionOrderExtensionHeading",
            "manageOrdersSupervisionOrderCourtDirection",
            "manageOrdersSupervisionOrderApprovalDate",
            "manageOrdersSupervisionOrderEndDate",
            "manageOrdersOrderCreatedDate",
            "manageOrdersPartyGrantedLeave",
            "manageOrdersChildNewSurname",
            "manageOrdersParentageApplicant",
            "manageOrdersHearingParty1",
            "manageOrdersHearingParty2",
            "manageOrdersPersonWhoseParenthoodIs",
            "manageOrdersParentageAction"
        );
    }
}
