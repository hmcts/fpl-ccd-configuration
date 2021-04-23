package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ManageOrderDocumentScopedFieldsCalculatorTest {

    private final ManageOrderDocumentScopedFieldsCalculator underTest = new ManageOrderDocumentScopedFieldsCalculator();

    @Test
    void calculate() {
        assertThat(underTest.calculate()).isEqualTo(
            List.of(
                "judgeAndLegalAdvisor",
                "manageOrdersApprovalDate",
                "orderAppliesToAllChildren",
                "children_label",
                "childSelector",
                "manageOrdersFurtherDirections",
                "orderPreview",
                "manageOrdersTitle",
                "manageOrdersDirections",
                "manageOrdersOperation",
                "manageOrdersType",
                "orderTempQuestions",
                "issuingDetailsSectionSubHeader",
                "childrenDetailsSectionSubHeader",
                "orderDetailsSectionSubHeader"
            )
        );
    }
}
