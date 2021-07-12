package uk.gov.hmcts.reform.fpl.service.orders;

import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.OrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedCaseManagementOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedGeneratedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedUrgentHearingOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

class ManageOrdersEventBuilderTest {
    private final DocumentReference document = mock(DocumentReference.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final List<Element<GeneratedOrder>> orders = wrapElements(order);

    private final SealedOrderHistoryService historyService = mock(SealedOrderHistoryService.class);
    private final AmendedGeneratedOrderFinder amendedGeneratedOrderFinder = mock(AmendedGeneratedOrderFinder.class);
    private final List<AmendedOrderFinder<? extends AmendableOrder>> finders = List.of(
        amendedGeneratedOrderFinder, mock(AmendedCaseManagementOrderFinder.class), mock(AmendedUrgentHearingOrderFinder.class),
        mock(AmendedCaseManagementOrderFinder.class));
    private final ManageOrdersEventBuilder underTest = new ManageOrdersEventBuilder(historyService, finders);

    @Test
    void buildAmended() {
        List<Element<Other>> selectedOthers = List.of(element(testOther("Other 1")));
        DocumentReference expectedDocument = testDocumentReference();
        GeneratedOrder generatedOrder = GeneratedOrder.builder()
            .document(expectedDocument)
            .type("Care order")
            .others(selectedOthers)
            .build();

        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(orders);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(GeneratedOrder.builder()
            .document(document)
            .build());
        when(amendedGeneratedOrderFinder.findOrderIfPresent(any(), any())).thenReturn(Optional.of(generatedOrder));

        Optional<ManageOrdersEvent> event = underTest.build(caseData, caseDataBefore);

        Optional<ManageOrdersEvent> expectedEvent = Optional.of(new AmendedOrderEvent(caseData, expectedDocument, "Care order", selectedOthers));
        assertThat(event).usingRecursiveComparison().isEqualTo(expectedEvent);
    }

    @Test
    void buildNonAmended() {
        List<Element<GeneratedOrder>> ordersBefore = List.of();
        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(ordersBefore);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(order);
        when(order.getDocument()).thenReturn(document);
        when(historyService.lastGeneratedOrder(caseData)).thenReturn(GeneratedOrder.builder()
            .document(document)
            .build());

        Optional<ManageOrdersEvent> event = underTest.build(caseData, caseDataBefore);

        Optional<ManageOrdersEvent> expectedEvent = Optional.of(new GeneratedOrderEvent(caseData, document));
        assertThat(event).usingRecursiveComparison().isEqualTo(expectedEvent);
    }
}
