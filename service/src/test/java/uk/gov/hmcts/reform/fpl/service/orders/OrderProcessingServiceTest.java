package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.AmendOrderService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderProcessingServiceTest {
    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);

    private final SealedOrderHistoryService historyService = mock(SealedOrderHistoryService.class);
    private final AmendOrderService amendmentService = mock(AmendOrderService.class);
    private final OrderProcessingService underTest = new OrderProcessingService(amendmentService, historyService);

    @BeforeEach
    void setUp() {
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
    }

    @Test
    void processAmendmentOperation() {
        when(eventData.getManageOrdersOperation()).thenReturn(OrderOperation.AMEND);

        Map<String, Object> amendmentData = Map.of("some amendment", "data");
        when(amendmentService.updateOrder(caseData)).thenReturn(amendmentData);

        assertThat(underTest.process(caseData)).isEqualTo(amendmentData);
    }

    @Test
    void processAmendmentOperationClosed() {
        when(eventData.getManageOrdersOperation()).thenReturn(null);
        when(eventData.getManageOrdersOperationClosedState()).thenReturn(OrderOperation.AMEND);

        Map<String, Object> amendmentData = Map.of("some amendment", "data");
        when(amendmentService.updateOrder(caseData)).thenReturn(amendmentData);

        assertThat(underTest.process(caseData)).isEqualTo(amendmentData);
    }

    @Test
    void processNonAmendmentOperation() {
        when(eventData.getManageOrdersOperation()).thenReturn(OrderOperation.CREATE);

        Map<String, Object> creationData = Map.of("some creation", "data");
        when(historyService.generate(caseData)).thenReturn(creationData);

        assertThat(underTest.process(caseData)).isEqualTo(creationData);
    }
}
