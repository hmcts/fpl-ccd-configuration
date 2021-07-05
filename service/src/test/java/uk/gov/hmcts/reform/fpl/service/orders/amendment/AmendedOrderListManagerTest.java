package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class AmendedOrderListManagerTest {
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID UHO_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");
    private static final LocalDate NOW = LocalDate.now();
    private static final LocalDate PAST = NOW.minusDays(1);
    private static final LocalDate DISTANT_PAST = NOW.minusDays(15);

    @Mock
    private UrgentHearingOrder uho;
    @Mock
    private StandardDirectionOrder sdo;
    @Mock
    private GeneratedOrder order;
    @Mock
    private HearingOrder cmo;
    @Captor
    private ArgumentCaptor<List<Element<? extends AmendableOrder>>> listCaptor;

    @Mock
    private DynamicListService listService;
    private AmendedOrderListManager underTest;

    @BeforeEach
    void setUp() {
        underTest = new AmendedOrderListManager(listService);
    }

    @Test
    void buildList() {
        String sdoLabel = "standard directions order";
        String generatedOrderLabel = "generated order";

        // hearing order and cmo label methods are not called as the comparator doesn't need to evaluate with those
        // methods
        when(sdo.asLabel()).thenReturn(sdoLabel);
        when(order.asLabel()).thenReturn(generatedOrderLabel);

        when(uho.amendableSortDate()).thenReturn(NOW);
        when(sdo.amendableSortDate()).thenReturn(PAST);
        when(order.amendableSortDate()).thenReturn(PAST);
        when(cmo.amendableSortDate()).thenReturn(DISTANT_PAST);

        when(sdo.isSealed()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(uho)
            .standardDirectionOrder(sdo)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        List<Element<? extends AmendableOrder>> amendableOrders = List.of(
            element(UHO_ID, uho), element(ORDER_ID, order), element(SDO_ID, sdo),
            element(CMO_ID, cmo)
        );

        DynamicList expectedAmendableOrderList = mock(DynamicList.class);

        when(listService.asDynamicList(listCaptor.capture(), any(), any())).thenReturn(expectedAmendableOrderList);

        Optional<DynamicList> builtAmendableOrderList = underTest.buildList(caseData);

        assertThat(builtAmendableOrderList).contains(expectedAmendableOrderList);
        assertThat(listCaptor.getValue()).isEqualTo(amendableOrders);
    }

    @Test
    void buildListWithoutSealedSDO() {
        when(uho.amendableSortDate()).thenReturn(NOW);
        when(order.amendableSortDate()).thenReturn(PAST);
        when(cmo.amendableSortDate()).thenReturn(DISTANT_PAST);

        when(sdo.isSealed()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(uho)
            .standardDirectionOrder(sdo)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        List<Element<? extends AmendableOrder>> amendableOrders = List.of(
            element(UHO_ID, uho), element(ORDER_ID, order),
            element(CMO_ID, cmo)
        );

        DynamicList expectedAmendableOrderList = mock(DynamicList.class);

        when(listService.asDynamicList(listCaptor.capture(), any(), any())).thenReturn(expectedAmendableOrderList);

        Optional<DynamicList> builtAmendableOrderList = underTest.buildList(caseData);

        assertThat(builtAmendableOrderList).contains(expectedAmendableOrderList);
        assertThat(listCaptor.getValue()).isEqualTo(amendableOrders);
    }

    @Test
    void buildListWhenSDOIsNull() {
        when(uho.amendableSortDate()).thenReturn(NOW);
        when(order.amendableSortDate()).thenReturn(PAST);
        when(cmo.amendableSortDate()).thenReturn(DISTANT_PAST);

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(uho)
            .standardDirectionOrder(null)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        List<Element<? extends AmendableOrder>> amendableOrders = List.of(
            element(UHO_ID, uho), element(ORDER_ID, order),
            element(CMO_ID, cmo)
        );

        DynamicList expectedAmendableOrderList = mock(DynamicList.class);

        when(listService.asDynamicList(listCaptor.capture(), any(), any())).thenReturn(expectedAmendableOrderList);

        Optional<DynamicList> builtAmendableOrderList = underTest.buildList(caseData);

        assertThat(builtAmendableOrderList).contains(expectedAmendableOrderList);
        assertThat(listCaptor.getValue()).isEqualTo(amendableOrders);
    }

    @Test
    void buildListWithoutUrgentHearingOrder() {
        when(sdo.amendableSortDate()).thenReturn(NOW);
        when(order.amendableSortDate()).thenReturn(PAST);
        when(cmo.amendableSortDate()).thenReturn(DISTANT_PAST);

        when(sdo.isSealed()).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(null)
            .standardDirectionOrder(sdo)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        List<Element<? extends AmendableOrder>> amendableOrders = List.of(
            element(SDO_ID, sdo), element(ORDER_ID, order), element(CMO_ID, cmo)
        );

        DynamicList expectedAmendableOrderList = mock(DynamicList.class);

        when(listService.asDynamicList(listCaptor.capture(), any(), any())).thenReturn(expectedAmendableOrderList);

        Optional<DynamicList> builtAmendableOrderList = underTest.buildList(caseData);

        assertThat(builtAmendableOrderList).contains(expectedAmendableOrderList);
        assertThat(listCaptor.getValue()).isEqualTo(amendableOrders);
    }

    @Test
    void buildListClosedState() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .build();

        Optional<DynamicList> builtAmendableOrderList = underTest.buildList(caseData);

        assertThat(builtAmendableOrderList).isEmpty();
    }

    @Test
    void getSelectedOrderForSDO() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        DocumentReference orderDocument = mock(DocumentReference.class);

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersAmendmentList(amendedOrderList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .standardDirectionOrder(sdo)
            .urgentHearingOrder(uho)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(SDO_ID);
        when(sdo.getOrderDoc()).thenReturn(orderDocument);

        assertThat(underTest.getSelectedOrder(caseData)).isEqualTo(orderDocument);
    }

    @Test
    void getSelectedOrderForUHO() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        DocumentReference orderDocument = mock(DocumentReference.class);

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersAmendmentList(amendedOrderList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .standardDirectionOrder(sdo)
            .urgentHearingOrder(uho)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(UHO_ID);
        when(uho.getOrder()).thenReturn(orderDocument);

        assertThat(underTest.getSelectedOrder(caseData)).isEqualTo(orderDocument);
    }

    @Test
    void getSelectedOrderForCMO() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        DocumentReference orderDocument = mock(DocumentReference.class);

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersAmendmentList(amendedOrderList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .standardDirectionOrder(sdo)
            .urgentHearingOrder(uho)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(CMO_ID);
        when(cmo.getOrder()).thenReturn(orderDocument);

        assertThat(underTest.getSelectedOrder(caseData)).isEqualTo(orderDocument);
    }

    @Test
    void getSelectedOrderForOrder() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        DocumentReference orderDocument = mock(DocumentReference.class);

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersAmendmentList(amendedOrderList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .standardDirectionOrder(sdo)
            .urgentHearingOrder(uho)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(ORDER_ID);
        when(order.getDocument()).thenReturn(orderDocument);

        assertThat(underTest.getSelectedOrder(caseData)).isEqualTo(orderDocument);
    }

    @Test
    void getSelectedOrderForOrderNotFound() {
        DynamicList amendedOrderList = mock(DynamicList.class);

        ManageOrdersEventData eventData = ManageOrdersEventData.builder()
            .manageOrdersAmendmentList(amendedOrderList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(eventData)
            .standardDirectionOrder(sdo)
            .urgentHearingOrder(uho)
            .orderCollection(List.of(element(ORDER_ID, order)))
            .sealedCMOs(List.of(element(CMO_ID, cmo)))
            .build();

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(UUID.fromString("44444444-4444-4444-4444-444444444444"));

        assertThatThrownBy(() -> underTest.getSelectedOrder(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Could not find amendable order with id \"44444444-4444-4444-4444-444444444444\"");
    }
}
