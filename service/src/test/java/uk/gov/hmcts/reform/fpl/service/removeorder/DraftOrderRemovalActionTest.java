package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class DraftOrderRemovalActionTest {

    public static final UUID ORDER_TO_REMOVE_ID = UUID.randomUUID();

    private DraftOrderRemovalAction underTest = new DraftOrderRemovalAction(
        new UpdateHearingOrderBundlesDrafts());

    @Test
    void isAcceptedOfDraftOrders() {
        RemovableOrder order = HearingOrder.builder().type(C21).status(SEND_TO_JUDGE).build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = HearingOrderType.class, mode = EXCLUDE, names = {"C21"})
    void isNotAcceptedWhenOrderTypeIsNotC21(HearingOrderType orderType) {
        RemovableOrder order = HearingOrder.builder().type(orderType).build();

        assertThat(underTest.isAccepted(order)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = CMOStatus.class, mode = EXCLUDE, names = {"SEND_TO_JUDGE"})
    void isNotAcceptedWhenDraftOrderStatusIsNotSendToJudge(CMOStatus status) {
        RemovableOrder order = HearingOrder.builder().status(status).build();

        assertThat(underTest.isAccepted(order)).isFalse();
    }

    @Test
    void isNotAcceptedIfNotHearingOrderClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovableDraftOrder() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        Element<HearingOrder> cmo = element(cmo(orderDocument));
        Element<HearingOrder> orderToRemove = element(blankOrder(orderDocument, SEND_TO_JUDGE));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(
                List.of(element(HearingOrdersBundle.builder().orders(newArrayList(cmo, orderToRemove)).build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        underTest.populateCaseFields(caseData, caseDetailsMap, orderToRemove.getId(), orderToRemove.getValue());

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "showRemoveCMOFieldsFlag",
                "showReasonFieldFlag")
            .containsExactly(orderDocument,
                orderToRemove.getValue().getTitle(),
                StringUtils.EMPTY,
                NO.getValue());
    }

    @Test
    void shouldThrowAnExceptionIfHearingOrderBundleContainingDraftOrderToRemoveCannotBeFound() {
        Element<HearingOrder> draftOrder = element(HearingOrder.builder().type(C21).status(SEND_TO_JUDGE).build());
        Element<HearingOrdersBundle> bundleElement =
            element(HearingOrdersBundle.builder().orders(newArrayList(draftOrder)).build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(bundleElement)).build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, ORDER_TO_REMOVE_ID, draftOrder.getValue()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(format("Failed to find hearing order bundle that contains order %s",
                ORDER_TO_REMOVE_ID));
    }

    @Test
    void shouldRemoveDraftOrderWhenSelectedOrderIdFoundInHearingOrdersBundle() {
        DocumentReference order = testDocumentReference();

        Element<HearingOrder> orderToBeRemoved = element(ORDER_TO_REMOVE_ID, cmo(testDocumentReference()));
        Element<HearingOrder> anotherOrder = element(cmo(order));

        Element<HearingOrdersBundle> hearingOrdersBundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(anotherOrder, orderToBeRemoved))
            .build());

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(new HashMap<>()).build());
        underTest.remove(caseData, caseDetailsMap, ORDER_TO_REMOVE_ID, orderToBeRemoved.getValue());

        List<Object> expectedBundles = List.of(element(hearingOrdersBundle.getId(),
            HearingOrdersBundle.builder().orders(newArrayList(anotherOrder)).build()));

        assertThat(caseDetailsMap.get("hearingOrdersBundlesDrafts")).isEqualTo(expectedBundles);
    }

    private HearingOrder cmo(DocumentReference order) {
        return HearingOrder.builder()
            .type(DRAFT_CMO)
            .status(DRAFT)
            .order(order)
            .dateSent(LocalDate.now().minusWeeks(1))
            .hearing("Draft Case management hearing sent on "
                + formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), "d MMMM yyyy"))
            .build();
    }

    private HearingOrder blankOrder(DocumentReference orderDocument, CMOStatus status) {
        return HearingOrder.builder()
            .type(C21)
            .status(status)
            .dateSent(LocalDate.now().minusDays(1))
            .order(orderDocument).build();
    }
}
