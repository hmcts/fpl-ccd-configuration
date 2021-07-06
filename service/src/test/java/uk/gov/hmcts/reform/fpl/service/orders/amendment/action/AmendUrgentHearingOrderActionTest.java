package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmendUrgentHearingOrderActionTest {

    private static final LocalDate AMENDED_DATE = LocalDate.of(12, 12, 12);

    private final CaseData caseData = mock(CaseData.class);

    private final Time time = new FixedTime(LocalDateTime.of(AMENDED_DATE, LocalTime.NOON));
    private final AmendUrgentHearingOrderAction underTest = new AmendUrgentHearingOrderAction(time);

    @Test
    void acceptValid() {
        ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
        DynamicList amendedOrderList = mock(DynamicList.class);
        UUID selectedID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(selectedID);

        assertThat(underTest.accept(caseData)).isTrue();
    }

    @Test
    void acceptInvalid() {
        ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
        DynamicList amendedOrderList = mock(DynamicList.class);
        UUID selectedID = UUID.fromString("12312311-1231-1231-1231-123123123123");

        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(selectedID);

        assertThat(underTest.accept(caseData)).isFalse();
    }

    @Test
    void applyAmendedOrder() {
        DocumentReference originalOrder = mock(DocumentReference.class);
        DocumentReference amendedOrder = mock(DocumentReference.class);

        UrgentHearingOrder uho = UrgentHearingOrder.builder().order(originalOrder).build();

        when(caseData.getUrgentHearingOrder()).thenReturn(uho);

        UrgentHearingOrder amendedUHO = uho.toBuilder().amendedDate(AMENDED_DATE).order(amendedOrder).build();

        assertThat(underTest.applyAmendedOrder(caseData, amendedOrder)).isEqualTo(
            Map.of("urgentHearingOrder", amendedUHO)
        );
    }
}
