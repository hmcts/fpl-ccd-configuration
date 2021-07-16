package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

@ExtendWith(MockitoExtension.class)
class ManageOrdersEventBuilderTest {
    private final DocumentReference document = mock(DocumentReference.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);
    private final List<Element<GeneratedOrder>> orders = wrapElements(order);

    @Mock
    private SealedOrderHistoryService historyService;

    @Mock
    private AmendableOrder amendableOrder;

    @Mock
    private AmendedOrderFinder<AmendableOrder> finder;

    private ManageOrdersEventBuilder underTest;

    @BeforeEach
    void setUp() {
        underTest = new ManageOrdersEventBuilder(historyService, List.of(finder));
    }

    @Test
    void buildAmended() {
        List<Element<Other>> selectedOthers = List.of(element(testOther("Other 1")));
        DocumentReference expectedDocument = testDocumentReference();

        when(caseData.getOrderCollection()).thenReturn(orders);
        when(caseDataBefore.getOrderCollection()).thenReturn(orders);
        when(amendableOrder.getDocument()).thenReturn(expectedDocument);
        when(amendableOrder.getAmendedOrderType()).thenReturn("Care order");
        when(amendableOrder.getSelectedOthers()).thenReturn(selectedOthers);

        when(finder.findOrderIfPresent(caseData, caseDataBefore)).thenReturn(Optional.of(amendableOrder));

        ManageOrdersEvent event = underTest.build(caseData, caseDataBefore);

        ManageOrdersEvent expectedEvent = new AmendedOrderEvent(caseData, expectedDocument,
            "Care order", selectedOthers);
        assertThat(event).isEqualTo(expectedEvent);
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

        ManageOrdersEvent event = underTest.build(caseData, caseDataBefore);
        ManageOrdersEvent expectedEvent = new GeneratedOrderEvent(caseData, document);
        assertThat(event).isEqualTo(expectedEvent);
    }
}
