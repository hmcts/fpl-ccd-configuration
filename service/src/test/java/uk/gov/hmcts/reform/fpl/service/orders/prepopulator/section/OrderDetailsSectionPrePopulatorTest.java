package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.IsFinalOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderDetailsSectionPrePopulatorTest {

    private final OrderSectionPrePopulator underTest = new OrderDetailsSectionPrePopulator();
    private final Order mockOrder = mock(Order.class);
    private static final String ORDER_NAME = "Mock order pls ignore";
    private static final OrderTempQuestions ORDER_TEMP_QUESTIONS = OrderTempQuestions.builder().build();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderSection.ORDER_DETAILS);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(mockOrder)
                .orderTempQuestions(ORDER_TEMP_QUESTIONS)
                .build()
            )
            .build();

        when(mockOrder.getHistoryTitle()).thenReturn(ORDER_NAME);
        when(mockOrder.getIsFinalOrder()).thenReturn(IsFinalOrder.NO);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of(
            "orderDetailsSectionSubHeader", ORDER_NAME,
            "orderTempQuestions", ORDER_TEMP_QUESTIONS));
    }
}
