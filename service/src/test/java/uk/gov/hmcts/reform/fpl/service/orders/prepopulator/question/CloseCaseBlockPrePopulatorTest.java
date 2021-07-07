package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CloseCaseBlockPrePopulatorTest {

    private static final String ORDER_QUESTION = "orderTempQuestions";
    private static final Order FINAL_ORDER_TYPE = Order.C32_CARE_ORDER;
    private static final Order NON_FINAL_ORDER_TYPE = Order.C23_EMERGENCY_PROTECTION_ORDER;
    private static final Order MAYBE_FINAL_ORDER_TYPE = Order.C32B_DISCHARGE_OF_CARE_ORDER;
    private final OrderTempQuestions initialOrderQuestions = getOrderQuestions();

    @Mock
    private ChildrenService childrenService;

    @InjectMocks
    private CloseCaseBlockPrePopulator underTest;

    @Test
    void shouldAcceptCloseCaseQuestionBlock() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.CLOSE_CASE);
    }

    @Test
    void shouldAllowCaseClosureWhenOrderIsFinalAndAllChildrenHaveFinalOrders() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(wrapElements(childWithFinalOrder(), childWithFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);

        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("YES").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    @Test
    void shouldNotAllowCaseClosureWhenOrderIsFinalButNotAllChildrenHaveFinalOrders() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(wrapElements(childWithFinalOrder(), childWithoutFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);
        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("NO").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    @Test
    void shouldNotAllowCaseClosureWhenOrderIsNotFinal() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(NON_FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData)).thenReturn(wrapElements(childWithFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);

        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("NO").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    @Test
    void shouldAllowCaseClosureWhenUserDecidedOrderIsFinalAndAllChildrenHaveFinalOrders() {

        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(MAYBE_FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .manageOrdersIsFinalOrder("Yes")
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(wrapElements(childWithFinalOrder(), childWithFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);

        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("YES").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    @Test
    void shouldNotAllowCaseClosureWhenUserDecidedOrderIsNotFinalAndAllChildrenHaveFinalOrders() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(MAYBE_FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .manageOrdersIsFinalOrder("No")
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(wrapElements(childWithFinalOrder(), childWithFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);

        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("NO").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    @Test
    void shouldNotAllowCaseClosureWhenUserDecidedOrderIsFinalButNotAllChildrenHaveFinalOrders() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(MAYBE_FINAL_ORDER_TYPE)
                .orderTempQuestions(initialOrderQuestions)
                .manageOrdersIsFinalOrder("Yes")
                .build())
            .build();

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(wrapElements(childWithFinalOrder(), childWithoutFinalOrder()));

        final Map<String, Object> actual = underTest.prePopulate(caseData);

        final OrderTempQuestions expectedOrderQuestions = initialOrderQuestions.toBuilder().closeCase("NO").build();

        assertThat(actual).containsExactly(entry(ORDER_QUESTION, expectedOrderQuestions));
    }

    private static OrderTempQuestions getOrderQuestions() {
        return OrderTempQuestions.builder()
            .closeCase("YES")
            .approver(randomAlphanumeric(10))
            .build();
    }

    private static Child childWithFinalOrder() {
        return Child.builder()
            .finalOrderIssued("Yes")
            .build();
    }

    private static Child childWithoutFinalOrder() {
        return Child.builder()
            .finalOrderIssued("No")
            .build();
    }

}
