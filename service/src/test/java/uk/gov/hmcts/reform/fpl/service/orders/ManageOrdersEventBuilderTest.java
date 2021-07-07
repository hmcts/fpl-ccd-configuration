package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ManageOrdersEventBuilderTest {
    private final DocumentReference document = mock(DocumentReference.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final List<Element<GeneratedOrder>> orders = wrapElements(order);

    private final SealedOrderHistoryService historyService = mock(SealedOrderHistoryService.class);
    private final ManageOrdersEventBuilder underTest = new ManageOrdersEventBuilder(historyService);

    @Test
    void buildAmended() {
        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(orders);

        Optional<GeneratedOrderEvent> event = underTest.build(caseData, caseDataBefore);

        assertThat(event).isEmpty();
    }

    @Test
    void buildNonAmended() {
        List<Element<GeneratedOrder>> ordersBefore = List.of();
        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(ordersBefore);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(order);
        when(order.getDocument()).thenReturn(document);

        Optional<GeneratedOrderEvent> event = underTest.build(caseData, caseDataBefore);

        assertThat(event).isPresent();
        assertThat(event.get().getCaseData()).isEqualTo(caseData);
        assertThat(event.get().getOrderDocument()).isEqualTo(document);
    }
}
