package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialGeneratedOrders;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AmendGeneratedOrderActionTest {
    private static final LocalDate AMENDED_DATE = LocalDate.of(12, 12, 12);
    private static final DocumentReference ORIGINAL_DOCUMENT = mock(DocumentReference.class);
    private static final DocumentReference AMENDED_DOCUMENT = mock(DocumentReference.class);
    List<Element<Other>> selectedOthers = Collections.emptyList();

    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
    private final DynamicList amendedOrderList = mock(DynamicList.class);
    private final UUID selectedOrderId = UUID.randomUUID();

    private final Time time = new FixedTime(LocalDateTime.of(AMENDED_DATE, LocalTime.NOON));
    private final AmendGeneratedOrderAction underTest = new AmendGeneratedOrderAction(time);

    @BeforeEach
    void setUp() {
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(selectedOrderId);
    }

    @Test
    void acceptValid() {
        List<Element<GeneratedOrder>> orders = List.of(element(selectedOrderId, mock(GeneratedOrder.class)));

        when(caseData.getAllOrderCollections()).thenReturn(orders);

        assertThat(underTest.accept(caseData)).isTrue();
    }

    @Test
    void acceptInvalid() {
        List<Element<GeneratedOrder>> orders = wrapElements(mock(GeneratedOrder.class));

        when(caseData.getAllOrderCollections()).thenReturn(orders);

        assertThat(underTest.accept(caseData)).isFalse();
    }

    @Test
    void applyAmendedOrder() {
        GeneratedOrder orderToAmend = GeneratedOrder.builder().document(ORIGINAL_DOCUMENT).build();
        Element<GeneratedOrder> nonAmendedOrder1 = element(mock(GeneratedOrder.class));
        Element<GeneratedOrder> nonAmendedOrder2 = element(mock(GeneratedOrder.class));

        when(caseData.getOrderCollection()).thenReturn(new ArrayList<>(List.of(
            nonAmendedOrder1, element(selectedOrderId, orderToAmend), nonAmendedOrder2
        )));

        GeneratedOrder amendedOrder = orderToAmend.toBuilder()
            .document(AMENDED_DOCUMENT)
            .amendedDate(AMENDED_DATE)
            .others(Collections.emptyList())
            .build();

        List<Element<GeneratedOrder>> amendedOrders = List.of(
            nonAmendedOrder1, element(selectedOrderId, amendedOrder), nonAmendedOrder2
        );

        assertThat(underTest.applyAmendedOrder(caseData, AMENDED_DOCUMENT, selectedOthers)).isEqualTo(
            Map.of("orderCollection", amendedOrders)
        );
    }

    @Test
    void applyAmendedConfidentialOrder() {
        GeneratedOrder orderToAmend = GeneratedOrder.builder().document(ORIGINAL_DOCUMENT).build();
        Element<GeneratedOrder> nonAmendedOrder1 = element(mock(GeneratedOrder.class));
        Element<GeneratedOrder> nonAmendedOrder2 = element(mock(GeneratedOrder.class));

        when(caseData.getOrderCollection()).thenReturn(List.of());
        when(caseData.getConfidentialOrders()).thenReturn(ConfidentialGeneratedOrders.builder()
            .orderCollectionCTSC(new ArrayList<>(List.of(
                nonAmendedOrder1, element(selectedOrderId, orderToAmend), nonAmendedOrder2
            ))).build());

        GeneratedOrder amendedOrder = orderToAmend.toBuilder()
            .document(AMENDED_DOCUMENT)
            .amendedDate(AMENDED_DATE)
            .others(Collections.emptyList())
            .build();

        List<Element<GeneratedOrder>> amendedOrders = List.of(
            nonAmendedOrder1, element(selectedOrderId, amendedOrder), nonAmendedOrder2
        );

        assertThat(underTest.applyAmendedOrder(caseData, AMENDED_DOCUMENT, selectedOthers)).isEqualTo(
            Map.of("orderCollectionCTSC", amendedOrders)
        );
    }
}
