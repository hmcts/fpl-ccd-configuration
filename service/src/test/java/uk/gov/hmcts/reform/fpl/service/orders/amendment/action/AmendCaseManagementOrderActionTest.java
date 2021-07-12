package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
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

@MockitoSettings(strictness = Strictness.LENIENT)
class AmendCaseManagementOrderActionTest {
    private static final LocalDate AMENDED_DATE = LocalDate.of(12, 12, 12);
    private static final DocumentReference ORIGINAL_DOCUMENT = mock(DocumentReference.class);
    private static final DocumentReference AMENDED_DOCUMENT = mock(DocumentReference.class);
    List<Element<Other>> selectedOthers = Collections.emptyList();


    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);
    private final DynamicList amendedOrderList = mock(DynamicList.class);
    private final UUID selectedOrderId = UUID.randomUUID();

    private final Time time = new FixedTime(LocalDateTime.of(AMENDED_DATE, LocalTime.NOON));
    private final AmendCaseManagementOrderAction underTest = new AmendCaseManagementOrderAction(time);

    @BeforeEach
    void setUp() {
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(selectedOrderId);
    }

    @Test
    void acceptValid() {
        List<Element<HearingOrder>> orders = List.of(element(selectedOrderId, mock(HearingOrder.class)));

        when(caseData.getSealedCMOs()).thenReturn(orders);

        assertThat(underTest.accept(caseData)).isTrue();
    }

    @Test
    void acceptInvalid() {
        List<Element<HearingOrder>> orders = wrapElements(mock(HearingOrder.class));

        when(caseData.getSealedCMOs()).thenReturn(orders);

        assertThat(underTest.accept(caseData)).isFalse();
    }

    @Test
    void applyAmendedOrder() {
        HearingOrder cmoToAmend = HearingOrder.builder().order(ORIGINAL_DOCUMENT).build();
        Element<HearingOrder> ignoredCMO1 = element(mock(HearingOrder.class));
        Element<HearingOrder> ignoredCMO2 = element(mock(HearingOrder.class));

        when(caseData.getSealedCMOs()).thenReturn(new ArrayList<>(List.of(
            ignoredCMO1, element(selectedOrderId, cmoToAmend), ignoredCMO2
        )));

        HearingOrder amendedCMO = cmoToAmend.toBuilder()
            .order(AMENDED_DOCUMENT)
            .amendedDate(AMENDED_DATE)
            .build();

        List<Element<HearingOrder>> amendedOrders = List.of(
            ignoredCMO1, element(selectedOrderId, amendedCMO), ignoredCMO2
        );

        assertThat(underTest.applyAmendedOrder(caseData, AMENDED_DOCUMENT, selectedOthers)).isEqualTo(
            Map.of("sealedCMOs", amendedOrders)
        );
    }
}
