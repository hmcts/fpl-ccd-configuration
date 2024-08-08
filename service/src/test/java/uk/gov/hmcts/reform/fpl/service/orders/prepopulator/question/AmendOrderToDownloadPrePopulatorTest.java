package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.exceptions.NoDocumentException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class AmendOrderToDownloadPrePopulatorTest {
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID UHO_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

    private final UrgentHearingOrder uho = mock(UrgentHearingOrder.class);
    private final StandardDirectionOrder sdo = mock(StandardDirectionOrder.class);
    private final GeneratedOrder order = mock(GeneratedOrder.class);
    private final HearingOrder cmo = mock(HearingOrder.class);


    private final AmendOrderToDownloadPrePopulator underTest = new AmendOrderToDownloadPrePopulator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.ORDER_TO_AMEND);
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

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of("manageOrdersOrderToAmend", orderDocument));
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

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of("manageOrdersOrderToAmend", orderDocument));
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

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of("manageOrdersOrderToAmend", orderDocument));
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
        when(order.getDocumentOrDocumentConfidential()).thenReturn(orderDocument);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of("manageOrdersOrderToAmend", orderDocument));
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

        assertThatThrownBy(() -> underTest.prePopulate(caseData))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Could not find amendable order with id \"44444444-4444-4444-4444-444444444444\"");
    }

    @Test
    void shouldThrowNoDocumentException() {
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

        when(amendedOrderList.getValueCodeAsUUID()).thenReturn(CMO_ID);
        when(order.getDocument()).thenReturn(null);
        assertThatThrownBy(() -> underTest.prePopulate(caseData))
            .isInstanceOf(NoDocumentException.class)
            .hasMessage("Document with id " + CMO_ID + " not found");
    }
}
