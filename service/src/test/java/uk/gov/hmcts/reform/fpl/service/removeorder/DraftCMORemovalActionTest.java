package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DraftCMORemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID HEARING_ID = UUID.randomUUID();
    private static final UUID ANOTHER_HEARING_ID = UUID.randomUUID();
    private static final UUID HEARING_ORDER_BUNDLE_ID_ONE = UUID.randomUUID();
    private static final UUID HEARING_ORDER_BUNDLE_ID_TWO = UUID.randomUUID();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.now();

    @Mock
    private DraftOrderService draftOrderService;

    @Mock
    private UpdateCMOHearing updateCMOHearing;

    @Mock
    private UpdateHearingOrderBundlesDrafts updateOrderBundles;

    @InjectMocks
    private DraftCMORemovalAction underTest;

    @Test
    void isAcceptedOfDraftCaseManagementOrders() {
        RemovableOrder order = HearingOrder.builder().type(DRAFT_CMO).build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @Test
    void isAcceptedOfAgreedDraftCaseManagementOrder() {
        RemovableOrder order = HearingOrder.builder().type(AGREED_CMO).build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @Test
    void isNotAcceptedC21HearingOrder() {
        RemovableOrder order = HearingOrder.builder().type(C21).build();

        assertThat(underTest.isAccepted(order)).isFalse();
    }

    @Test
    void isAcceptedWhenHearingOrderTypeIsNull() {
        RemovableOrder order = HearingOrder.builder().build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedCMOAndHearingLinkedByCMOId() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        HearingOrder removedOrder = cmo(orderDocument);

        HearingBooking hearingToBeUnlinked = hearing(TO_REMOVE_ORDER_ID);

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HearingOrdersBundle.builder()
                    .orders(List.of(
                        element(TO_REMOVE_ORDER_ID, removedOrder)
                    )).build())
            ))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(ANOTHER_HEARING_ID)),
                element(UUID.randomUUID(), hearingToBeUnlinked)
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        when(updateCMOHearing.getHearingToUnlink(caseData, TO_REMOVE_ORDER_ID, removedOrder))
            .thenReturn(hearing());

        underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "hearingToUnlink",
                "showRemoveCMOFieldsFlag",
                "showReasonFieldFlag")
            .containsExactly(orderDocument,
                "Draft case management order",
                hearingToBeUnlinked.toLabel(),
                YES.getValue(),
                NO.getValue());
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedCMOAndHearingLinkedByLabel() {
        LocalDateTime startDate = HEARING_START_DATE.plusDays(3);
        DocumentReference orderDocument = DocumentReference.builder().build();
        HearingOrder removedOrder = cmo(orderDocument, startDate);
        HearingBooking hearingToBeUnlinked = hearing(UUID.randomUUID(), startDate);

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HearingOrdersBundle.builder()
                    .orders(List.of(
                        element(TO_REMOVE_ORDER_ID, removedOrder)
                    )).build())
            ))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing()),
                element(UUID.randomUUID(), hearingToBeUnlinked)
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        when(updateCMOHearing.getHearingToUnlink(caseData, TO_REMOVE_ORDER_ID, removedOrder))
            .thenReturn(hearingToBeUnlinked);

        underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "hearingToUnlink",
                "showRemoveCMOFieldsFlag",
                "showReasonFieldFlag")
            .containsExactly(orderDocument,
                "Draft case management order",
                hearingToBeUnlinked.toLabel(),
                YES.getValue(),
                NO.getValue());
    }

    @Test
    void shouldThrowAnExceptionIfUniqueHearingNotFound() {
        HearingOrder removedOrder = cmo(HEARING_START_DATE);

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HearingOrdersBundle.builder()
                    .orders(List.of(
                        element(TO_REMOVE_ORDER_ID, removedOrder)
                    )).build())
            ))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(UUID.randomUUID())),
                element(ANOTHER_HEARING_ID, hearing(UUID.randomUUID()))
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID,
            removedOrder))
            .usingRecursiveComparison()
            .isEqualTo(unexpectedNumberOfCMOsRemovedException(TO_REMOVE_ORDER_ID, 2));
    }

    @Test
    void shouldRemoveOrderWhenNoMatchingIDButMatchingHearingLabel() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        HearingOrder cmoToRemove = cmo(differentStartDate).toBuilder().type(DRAFT_CMO).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_ORDER_BUNDLE_ID_ONE, HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(TO_REMOVE_ORDER_ID, cmoToRemove)
                    )).build())
            ))
            .draftUploadedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmoToRemove)))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null, differentStartDate)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID)));

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, element(TO_REMOVE_ORDER_ID, cmoToRemove)))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove);

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(null, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            )
        ));
    }

    @Test
    void shouldRemoveCaseManagementOrderWhenOtherCMOisPresent() {
        HearingOrder draftCMO = HearingOrder.builder().type(DRAFT_CMO).build();

        Element<HearingOrdersBundle> selectedBundle = element(HEARING_ORDER_BUNDLE_ID_ONE,
            HearingOrdersBundle.builder()
                .orders(newArrayList(
                    element(TO_REMOVE_ORDER_ID, draftCMO),
                    element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO)
                )).build());

        Element<HearingOrdersBundle> anotherBundle = element(HEARING_ORDER_BUNDLE_ID_TWO,
            HearingOrdersBundle.builder()
                .orders(newArrayList(
                    element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, draftCMO)
                )).build());
        List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = List.of(selectedBundle, anotherBundle);

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
            .draftUploadedCMOs(newArrayList(
                element(TO_REMOVE_ORDER_ID, draftCMO),
                element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO)))
            .hearingDetails(newArrayList(
                element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            )).build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID)));

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, element(TO_REMOVE_ORDER_ID, draftCMO)))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, draftCMO);

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", updatedHearings,
            "draftUploadedCMOs", newArrayList(element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO))
        );

        verify(updateOrderBundles).update(caseDetailsMap, hearingOrdersBundlesDrafts, selectedBundle);
        assertThat(caseDetailsMap).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldRemoveCaseManagementOrderWhenPresentOnDraftCaseManagementOrdersOnly() {
        HearingOrder draftCMO = HearingOrder.builder().type(DRAFT_CMO).build();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(
                element(TO_REMOVE_ORDER_ID, draftCMO),
                element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO)))
            .hearingDetails(newArrayList(
                element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            )).build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID)));

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, element(TO_REMOVE_ORDER_ID, draftCMO)))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, draftCMO);

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", updatedHearings,
            "draftUploadedCMOs", newArrayList(element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO))
        );

        assertThat(caseDetailsMap).containsAllEntriesOf(expectedData);
    }

    @Test
    void shouldRemoveHearingAssociationWithARemovedCaseManagementOrder() {
        HearingOrder draftCMO = HearingOrder.builder().type(DRAFT_CMO).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_ORDER_BUNDLE_ID_ONE, HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(TO_REMOVE_ORDER_ID, draftCMO)
                    )).build())
            ))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(TO_REMOVE_ORDER_ID))))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
            element(ANOTHER_HEARING_ID, hearing(null)));

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, element(TO_REMOVE_ORDER_ID, draftCMO)))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, draftCMO);

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", updatedHearings)
        );
    }

    @Test
    void shouldThrowAnExceptionIfHearingOrderBundleContainingRemovedCMOCannotBeFound() {
        HearingOrder draftCMO = HearingOrder.builder().type(DRAFT_CMO).build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(
                element(HEARING_ORDER_BUNDLE_ID_ONE, HearingOrdersBundle.builder()
                    .orders(newArrayList()).build())
            )).build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, draftCMO))
            .usingRecursiveComparison()
            .isEqualTo(new IllegalStateException(format("Failed to find hearing order bundle that contains order %s",
                TO_REMOVE_ORDER_ID)));
    }

    @Test
    void shouldThrowAnExceptionIfDraftUploadedCMOContainingRemovedCMOCannotBeFound() {
        HearingOrder draftCMO = HearingOrder.builder().type(DRAFT_CMO).build();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(
                element(ANOTHER_DRAFT_CASE_MANAGEMENT_ORDER_ID, draftCMO)))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, draftCMO))
            .usingRecursiveComparison()
            .isEqualTo(new IllegalStateException(format("Failed to find hearing order that contains order %s",
                TO_REMOVE_ORDER_ID)));
    }

    @Test
    void shouldRemoveDraftCaseManagementOrderAndUnlinkHearing() {
        DocumentReference order = testDocumentReference();
        Element<HearingOrder> orderToBeRemoved = element(TO_REMOVE_ORDER_ID, cmo(testDocumentReference()));

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(
                orderToBeRemoved,
                element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo(order))
            ))
            .hearingDetails(newArrayList(
                element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        List<Element<HearingOrdersBundle>> ordersBundle = ElementUtils.wrapElements(HearingOrdersBundle.builder()
            .hearingId(HEARING_ID)
            .build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
        );

        when(draftOrderService.migrateCmoDraftToOrdersBundles(caseData)).thenReturn(ordersBundle);
        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, orderToBeRemoved))
            .thenReturn(updatedHearings);

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.removeDraftCaseManagementOrder(caseData, caseDetailsMap, orderToBeRemoved);

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", updatedHearings,
            "draftUploadedCMOs", List.of(element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo(order))),
            "hearingOrdersBundlesDrafts", ordersBundle
        );

        assertThat(caseDetailsMap).isEqualTo(expectedData);
    }

    @Test
    void shouldRemoveDraftOrderWhenNoMatchingIDButMatchingHearingLabel() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        Element<HearingOrder> cmoToRemove = element(TO_REMOVE_ORDER_ID, cmo(differentStartDate));
        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(cmoToRemove))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        List<Element<HearingOrdersBundle>> ordersBundle = ElementUtils.wrapElements(HearingOrdersBundle.builder()
            .hearingId(HEARING_ID)
            .build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null, differentStartDate)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
        );

        when(draftOrderService.migrateCmoDraftToOrdersBundles(caseData)).thenReturn(ordersBundle);
        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, cmoToRemove)).thenReturn(updatedHearings);

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.removeDraftCaseManagementOrder(caseData, caseDetailsMap, cmoToRemove);

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", updatedHearings,
            "hearingOrdersBundlesDrafts", ordersBundle
            )
        );
    }

    @Test
    void shouldThrowAnExceptionIfDraftOrderToBeRemovedIsNotFound() {
        UUID alreadyRemovedOrderId = UUID.randomUUID();
        Element<HearingOrder> removedOrder = element(alreadyRemovedOrderId, cmo());

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder("reason for order removal")
            .draftUploadedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmo())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.removeDraftCaseManagementOrder(caseData, caseDetailsMap, removedOrder))
            .isInstanceOf(CMONotFoundException.class)
            .hasMessage("Failed to find draft case management order");
    }

    private HearingBooking hearing() {
        return hearing(null);
    }

    private HearingBooking hearing(UUID cmoId) {
        return hearing(cmoId, HEARING_START_DATE);
    }

    private HearingBooking hearing(UUID cmoId, LocalDateTime startDate) {
        return HearingBooking.builder()
            .caseManagementOrderId(cmoId)
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .build();
    }

    private HearingOrder cmo() {
        return cmo(testDocumentReference(), null, null);
    }

    private HearingOrder cmo(DocumentReference order) {
        return cmo(order, null, null);
    }

    private HearingOrder cmo(LocalDateTime startDate) {
        return cmo(null, startDate, null);
    }

    private HearingOrder cmo(DocumentReference order, LocalDateTime startDate) {
        return cmo(order, startDate, null);
    }

    private HearingOrder cmo(DocumentReference order, LocalDateTime startDate, String removalReason) {
        return HearingOrder.builder()
            .order(order)
            .hearing(startDate != null
                ? "Case management hearing, " + formatLocalDateTimeBaseUsingFormat(startDate, DATE)
                : null)
            .removalReason(removalReason)
            .build();
    }

    private UnexpectedNumberOfCMOsRemovedException unexpectedNumberOfCMOsRemovedException(UUID id, int links) {
        return new UnexpectedNumberOfCMOsRemovedException(
            id,
            format("CMO %s could not be linked to hearing by CMO id and there wasn't a unique link (%d links found) to "
                    + "a hearing with the same label",
                id, links)
        );
    }
}
