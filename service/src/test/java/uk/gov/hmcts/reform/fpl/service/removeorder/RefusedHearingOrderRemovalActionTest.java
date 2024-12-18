package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class RefusedHearingOrderRemovalActionTest {

    public static final UUID ORDER_TO_REMOVE_ID = UUID.randomUUID();

    private RefusedHearingOrderRemovalAction underTest = new RefusedHearingOrderRemovalAction();

    @Test
    void acceptsReturnedOrder() {
        RemovableOrder order = HearingOrder.builder().status(RETURNED).build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @Test
    void shouldRaiseExceptionIfReturnedHearingIdNotFound() {
        DocumentReference order = testDocumentReference();

        Element<HearingOrder> orderToBeRemoved = element(ORDER_TO_REMOVE_ID, returnedOrder(testDocumentReference()));
        Element<HearingOrder> anotherOrder = element(returnedOrder(order));

        CaseData caseData = CaseData.builder()
            .refusedHearingOrders(newArrayList(anotherOrder))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(new HashMap<>()).build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, ORDER_TO_REMOVE_ID,
            orderToBeRemoved.getValue()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to find refused hearing order");
    }

    @Test
    void shouldRemoveReturnedOrder() {
        DocumentReference order = testDocumentReference();

        Element<HearingOrder> orderToBeRemoved = element(ORDER_TO_REMOVE_ID, returnedOrder(testDocumentReference()));
        Element<HearingOrder> anotherOrder = element(returnedOrder(order));

        CaseData caseData = CaseData.builder()
            .refusedHearingOrders(newArrayList(orderToBeRemoved, anotherOrder))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(new HashMap<>()).build());
        underTest.remove(caseData, caseDetailsMap, ORDER_TO_REMOVE_ID, orderToBeRemoved.getValue());

        List<Object> expectedOrders = newArrayList(anotherOrder);

        assertThat(caseDetailsMap.get("refusedHearingOrders")).isEqualTo(expectedOrders);
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovableReturnedOrder() {
        DocumentReference order = testDocumentReference();

        Element<HearingOrder> orderToBeRemoved = element(ORDER_TO_REMOVE_ID, returnedOrder(testDocumentReference()));
        Element<HearingOrder> anotherOrder = element(returnedOrder(order));

        CaseData caseData = CaseData.builder()
            .refusedHearingOrders(newArrayList(orderToBeRemoved, anotherOrder))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(new HashMap<>()).build());

        underTest.populateCaseFields(caseData, caseDetailsMap, ORDER_TO_REMOVE_ID, orderToBeRemoved.getValue());

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "showRemoveCMOFieldsFlag",
                "showReasonFieldFlag")
            .containsExactly(orderToBeRemoved.getValue().getOrder(),
                orderToBeRemoved.getValue().getTitle(),
                StringUtils.EMPTY,
                NO.getValue());
    }

    private HearingOrder returnedOrder(DocumentReference order) {
        return HearingOrder.builder()
            .type(DRAFT_CMO)
            .status(RETURNED)
            .order(order)
            .dateSent(LocalDate.now().minusWeeks(1))
            .hearing("Draft Case management hearing sent on "
                + formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), "d MMMM yyyy"))
            .build();
    }
}
