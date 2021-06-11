package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class WhichOrdersBlockPrePopulatorTest {

    private static final String ORDER_LABEL = "order label";
    private final DischargeCareOrderService dischargeCareOrderService = mock(DischargeCareOrderService.class);
    private final WhichOrdersBlockPrePopulator underTest = new WhichOrdersBlockPrePopulator(
        dischargeCareOrderService
    );


    @Test
    void prePopulateForDischargeOfCare() {
        List<Element<Child>> children = wrapElements(mock(Child.class), mock(Child.class));

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C32B_DISCHARGE_OF_CARE_ORDER)
                .build())
            .children1(children)
            .build();

        List<GeneratedOrder> careOrders = List.of(
            order(Order.C32_CARE_ORDER.getHistoryTitle(), mock(Child.class)),
            order(Order.C32_CARE_ORDER.getHistoryTitle(), mock(Child.class))
        );

        when(dischargeCareOrderService.getCareOrders(caseData)).thenReturn(careOrders);
        when(dischargeCareOrderService.getOrdersLabel(careOrders)).thenReturn(ORDER_LABEL);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                "careOrderSelector", Selector.builder().count("12").build(),
                "orders_label", ORDER_LABEL
            )
        );
    }

    private static GeneratedOrder order(String type, Child... children) {
        return GeneratedOrder.builder()
            .type(type)
            .children(wrapElements(children))
            .build();
    }
}
