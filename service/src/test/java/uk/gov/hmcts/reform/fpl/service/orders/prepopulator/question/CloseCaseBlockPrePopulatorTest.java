package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CloseCaseBlockPrePopulatorTest {

    private static final UUID CHILD_ID_1 = UUID.randomUUID();
    private static final UUID CHILD_ID_2 = UUID.randomUUID();
    private static final Order FINAL_ORDER_TYPE = Order.C32_CARE_ORDER;
    private static final Order NON_FINAL_ORDER_TYPE = Order.C23_EMERGENCY_PROTECTION_ORDER;

    private final ChildrenService childrenService = mock(ChildrenService.class);

    private final CloseCaseBlockPrePopulator underTest = new CloseCaseBlockPrePopulator(childrenService);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.CLOSE_CASE);
    }

    @Test
    void doNotAskWhenAllChildrenHaveFinalOrderIssued() {
        CaseData caseData = CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(FINAL_ORDER_TYPE)
            .orderTempQuestions(OrderTempQuestions.builder().closeCase("YES").build())
            .build()
        ).build();

        when(childrenService.updateFinalOrderIssued(caseData)).thenReturn(List.of(
            element(CHILD_ID_1, Child.builder().finalOrderIssued("Yes").build())
        ));

        Map<String, Object> actual = underTest.prePopulate(caseData);

        assertThat(actual).isEqualTo(Map.of());
    }

    @Test
    void doNotAskWhenOtherThanFinalOrder() {
        CaseData caseData = CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(NON_FINAL_ORDER_TYPE)
            .orderTempQuestions(OrderTempQuestions.builder().closeCase("YES").build())
            .build()
        ).build();

        when(childrenService.updateFinalOrderIssued(caseData)).thenReturn(List.of(
            element(CHILD_ID_1, Child.builder().finalOrderIssued("Yes").build())
        ));

        Map<String, Object> actual = underTest.prePopulate(caseData);

        assertThat(actual).isEqualTo(Map.of());
    }

    @Test
    void askWhenNotAllChildrenHaveFinalOrderIssuedAndFinalOrder() {
        CaseData caseData = CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(FINAL_ORDER_TYPE)
            .orderTempQuestions(OrderTempQuestions.builder().closeCase("YES").build())
            .build()
        ).build();

        when(childrenService.updateFinalOrderIssued(caseData)).thenReturn(List.of(
            element(CHILD_ID_1, Child.builder().finalOrderIssued("Yes").build()),
            element(CHILD_ID_2, Child.builder().finalOrderIssued("No").build())
        ));

        OrderTempQuestions actual = (OrderTempQuestions) underTest.prePopulate(caseData).get("orderTempQuestions");
        OrderTempQuestions expected = OrderTempQuestions.builder().closeCase("NO").build();

        assertThat(actual).isEqualTo(expected);
    }
}
