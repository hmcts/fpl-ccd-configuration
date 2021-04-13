package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class OrderDetailsSectionPrePopulatorTest {

    private static final Order ORDER = mock(Order.class);
    private static final Child CHILD = mock(Child.class);
    private static final Selector SELECTOR = Selector.builder().selected(List.of(1, 2)).build();
    private static final String ORDER_NAME = "Mock order pls ignore";

    private final OrderSectionPrePopulator underTest = new OrderDetailsSectionPrePopulator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderSection.ORDER_DETAILS);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(CHILD, CHILD))
            .childSelector(SELECTOR)
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(ORDER).build())
            .build();

        when(ORDER.getHistoryTitle()).thenReturn(ORDER_NAME);

        Selector expectedSelector = Selector.builder().count("12").selected(List.of(1,2)).build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of(
            "orderDetailsSectionSubHeader", ORDER_NAME,
            "childSelector", expectedSelector
        ));
    }
}
