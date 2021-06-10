package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrderDocumentScopedFieldsCalculatorTest {

    private final ManageOrderDocumentScopedFieldsCalculator underTest = new ManageOrderDocumentScopedFieldsCalculator();

    @Test
    void calculate() {
        assertThat(underTest.calculate()).containsExactlyInAnyOrder(
            "manageOrdersApprovedAtHearing",
            "manageOrdersApprovedAtHearingList",
            "judgeAndLegalAdvisor",
            "manageOrdersApprovalDate",
            "orderAppliesToAllChildren",
            "children_label",
            "orders_label",
            "childSelector",
            "careOrderSelector",
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
            "orderPreview",
            "manageOrdersCloseCase",
            "manageOrdersCloseCaseWarning",
            "manageOrdersApprovalDateTime",
            "manageOrdersOperation",
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
            "manageOrdersCafcassOfficesWales"
        );
    }
}
