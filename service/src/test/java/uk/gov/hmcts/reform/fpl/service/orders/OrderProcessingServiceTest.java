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
    private static final Map<String, Object> AMENDMENT_DATA = Map.of("some amendment", "data");
    private static final Map<String, Object> CREATION_DATA = Map.of("some creation", "data");

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

        when(amendmentService.updateOrder(caseData)).thenReturn(AMENDMENT_DATA);

        assertThat(underTest.process(caseData)).isEqualTo(AMENDMENT_DATA);
    }

    @Test
    void processAmendmentOperationClosed() {
        when(eventData.getManageOrdersOperation()).thenReturn(null);
        when(eventData.getManageOrdersOperationClosedState()).thenReturn(OrderOperation.AMEND);

        when(amendmentService.updateOrder(caseData)).thenReturn(AMENDMENT_DATA);

        assertThat(underTest.process(caseData)).isEqualTo(AMENDMENT_DATA);
    }

    @Test
    void processNonAmendmentOperation() {
        when(eventData.getManageOrdersOperation()).thenReturn(OrderOperation.CREATE);

        when(historyService.generate(caseData)).thenReturn(CREATION_DATA);

        assertThat(underTest.process(caseData)).isEqualTo(CREATION_DATA);
    }
}
