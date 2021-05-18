package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrderDocumentScopedFieldsCalculatorTest {

    private final ManageOrderDocumentScopedFieldsCalculator underTest = new ManageOrderDocumentScopedFieldsCalculator();

    @Test
    void calculate() {
        assertThat(underTest.calculate()).containsExactlyInAnyOrder(
            "judgeAndLegalAdvisor",
            "manageOrdersApprovalDate",
            "orderAppliesToAllChildren",
            "children_label",
            "childSelector",
            "manageOrdersTitle",
            "manageOrdersDirections",
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
            "orderPreview",
            "manageOrdersApprovalDateTime",
            "manageOrdersOperation",
            "manageOrdersType",
            "orderTempQuestions",
            "issuingDetailsSectionSubHeader",
            "childrenDetailsSectionSubHeader",
            "orderDetailsSectionSubHeader"
        );
    }
}
