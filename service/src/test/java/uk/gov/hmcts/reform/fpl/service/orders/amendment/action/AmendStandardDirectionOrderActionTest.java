package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
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

class AmendStandardDirectionOrderActionTest {
    private static final LocalDate AMENDED_DATE = LocalDate.of(12, 12, 12);

    private final CaseData caseData = mock(CaseData.class);

    private final Time time = new FixedTime(LocalDateTime.of(AMENDED_DATE, LocalTime.NOON));
    private final AmendStandardDirectionOrderAction underTest = new AmendStandardDirectionOrderAction(time);

    @Test
    void acceptValid() {
        ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
        DynamicList list = mock(DynamicList.class);

        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(list);
        when(list.getValueCodeAsUUID()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        assertThat(underTest.accept(caseData)).isTrue();
    }

    @Test
    void acceptInvalid() {
        ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
        DynamicList list = mock(DynamicList.class);

        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(list);
        when(list.getValueCodeAsUUID()).thenReturn(UUID.fromString("22222222-2222-2222-2222-222222222222"));

        assertThat(underTest.accept(caseData)).isFalse();
    }

    @Test
    void applyAmendedOrder() {
        DocumentReference originalDocument = mock(DocumentReference.class);
        DocumentReference amendedDocument = mock(DocumentReference.class);

        StandardDirectionOrder sdo = StandardDirectionOrder.builder()
            .orderDoc(originalDocument)
            .build();

        when(caseData.getStandardDirectionOrder()).thenReturn(sdo);

        StandardDirectionOrder amendedSDO = sdo.toBuilder().amendedDate(AMENDED_DATE).orderDoc(amendedDocument).build();

        assertThat(underTest.applyAmendedOrder(caseData, amendedDocument)).isEqualTo(
            Map.of("standardDirectionOrder", amendedSDO)
        );
    }
}
