package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CMORemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private static final UUID CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID HEARING_ID = UUID.randomUUID();
    private static final UUID ANOTHER_HEARING_ID = UUID.randomUUID();

    private final CMORemovalAction underTest = new CMORemovalAction();

    @Test
    void isAcceptedIfCaseManagementOrder() {
        assertThat(underTest.isAccepted(mock(CaseManagementOrder.class))).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldNotRemoveHearingAssociationWithARemovedCaseManagementOrderWhenCannotMatchAssociatedHearing() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build())))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID,
                CaseManagementOrder.builder().build())))
            .hearingDetails(List.of(
                element(HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                        .build()),
                element(ANOTHER_HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(ANOTHER_CASE_MANAGEMENT_ORDER_ID)
                        .build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build());

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(
                    HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                        .build()),
                element(
                    ANOTHER_HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(ANOTHER_CASE_MANAGEMENT_ORDER_ID)
                        .build())),
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, CaseManagementOrder.builder().build()),
                element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().removalReason(REASON).build())
            )
        ));

    }

    @Test
    void shouldRemovedCaseManagementOrderWhenOtherOtherCMOisPresent() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(
                element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build()),
                element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, CaseManagementOrder.builder().build())))
            .hiddenCaseManagementOrders(null)
            .hearingDetails(null)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build());

        Map<String, List<?>> expectedData = new HashMap<>();
        expectedData.put("hearingDetails", List.of());
        expectedData.put("hiddenCaseManagementOrders", List.of(
            element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().removalReason(REASON).build())
        ));
        expectedData.put("sealedCMOs", List.of(
            element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, CaseManagementOrder.builder().build()))
        );

        assertThat(caseDetailsMap).isEqualTo(expectedData);
    }

    @Test
    void shouldRemovedCaseManagementOrderWhenNoHearing() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build())))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID,
                CaseManagementOrder.builder().build())))
            .hearingDetails(null)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build());

        Map<String, List<?>> expectedData = new HashMap<>();
        expectedData.put("hearingDetails", List.of());
        expectedData.put("hiddenCaseManagementOrders", List.of(
            element(ALREADY_REMOVED_ORDER_ID, CaseManagementOrder.builder().build()),
            element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().removalReason(REASON).build())
        ));

        assertThat(caseDetailsMap).isEqualTo(expectedData);
    }

    @Test
    void shouldRemoveHearingAssociationWithARemovedCaseManagementOrder() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build())))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID,
                CaseManagementOrder.builder().build())))
            .hearingDetails(List.of(
                element(HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                        .build()),
                element(ANOTHER_HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(TO_REMOVE_ORDER_ID)
                        .build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build());

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(
                    HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                        .build()),
                element(
                    ANOTHER_HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(null)
                        .build())),
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, CaseManagementOrder.builder().build()),
                element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().removalReason(REASON).build())
            )
        ));
    }

    @Test
    void shouldThrowExceptionIfOrderNotFound() {
        CaseManagementOrder emptyCaseManagementOrder = CaseManagementOrder.builder().build();

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, emptyCaseManagementOrder)))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, ALREADY_REMOVED_ORDER_ID,
            emptyCaseManagementOrder))
            .isInstanceOf(CMONotFoundException.class)
            .hasMessage("Failed to find order matching id %s", ALREADY_REMOVED_ORDER_ID);
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedCMOAndUnlinkedHearing() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        CaseManagementOrder removedOrder = CaseManagementOrder.builder()
            .order(orderDocument)
            .build();

        HearingBooking hearingToBeUnlinked = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .caseManagementOrderId(TO_REMOVE_ORDER_ID)
            .startDate(LocalDateTime.now())
            .build();

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                element(HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(ANOTHER_HEARING_ID)
                        .build()),
                element(UUID.randomUUID(), hearingToBeUnlinked)))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "hearingToUnlink",
                "showRemoveCMOFieldsFlag")
            .containsExactly(orderDocument,
                "Case management order",
                hearingToBeUnlinked.toLabel(),
                YES.getValue());
    }

    @Test
    void shouldThrowAnExceptionIfHearingBookingNotFound() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        CaseManagementOrder removedOrder = CaseManagementOrder.builder()
            .order(orderDocument)
            .build();

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                element(HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(UUID.randomUUID())
                        .build()),
                element(ANOTHER_HEARING_ID,
                    HearingBooking.builder()
                        .caseManagementOrderId(UUID.randomUUID())
                        .build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.populateCaseFields(caseData, caseDetailsMap, ALREADY_REMOVED_ORDER_ID,
            removedOrder))
            .isInstanceOf(HearingNotFoundException.class)
            .hasMessage("Could not find hearing matching id %s", ALREADY_REMOVED_ORDER_ID);
    }
}
