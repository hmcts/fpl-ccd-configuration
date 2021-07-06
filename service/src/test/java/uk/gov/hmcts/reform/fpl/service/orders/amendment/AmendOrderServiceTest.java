package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.action.AmendOrderAction;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmendOrderServiceTest {

    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);

    private final AmendedOrderStamper stamper = mock(AmendedOrderStamper.class);
    private final AmendOrderAction action = mock(AmendOrderAction.class);

    private final AmendOrderService underTest = new AmendOrderService(stamper, List.of(action));


    @BeforeEach
    void setUp() {
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
    }

    @Test
    void updateOrder() {
        DocumentReference uploadedOrder = mock(DocumentReference.class);
        DocumentReference amendedOrder = mock(DocumentReference.class);
        Map<String, Object> amendedFields = Map.of("amendedCaseField", "some amended field");

        when(eventData.getManageOrdersAmendedOrder()).thenReturn(uploadedOrder);
        when(action.accept(caseData)).thenReturn(true);
        when(stamper.amendDocument(uploadedOrder)).thenReturn(amendedOrder);
        when(action.applyAmendedOrder(caseData, amendedOrder)).thenReturn(amendedFields);

        assertThat(underTest.updateOrder(caseData)).isEqualTo(amendedFields);
    }

    @Test
    void updateOrderNoActionFound() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        String orderId = "some id";

        when(action.accept(caseData)).thenReturn(false);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCode()).thenReturn(orderId);

        assertThatThrownBy(() -> underTest.updateOrder(caseData))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Could not find action to amend order for order with id \"some id\"");
    }
}
