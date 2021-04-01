package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class SealedCMORemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private static final UUID CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID HEARING_ID = UUID.randomUUID();
    private static final UUID ANOTHER_HEARING_ID = UUID.randomUUID();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.now();

    @Mock
    private UpdateCMOHearing updateCMOHearing;

    @InjectMocks
    private SealedCMORemovalAction underTest;

    @Test
    void isAcceptedOfAgreedCaseManagementOrders() {
        RemovableOrder order = HearingOrder.builder().status(CMOStatus.APPROVED).build();

        assertThat(underTest.isAccepted(order)).isTrue();
    }

    @Test
    void isNotAcceptedC21HearingOrder() {
        RemovableOrder order = HearingOrder.builder().type(C21).build();

        assertThat(underTest.isAccepted(order)).isFalse();
    }

    @Test
    void isNotAcceptedDraftCaseManagementOrder() {
        RemovableOrder order = HearingOrder.builder().status(CMOStatus.DRAFT).build();

        assertThat(underTest.isAccepted(order)).isFalse();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldRemoveOrderWhenNoMatchingIDButMatchingHearingLabel() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        HearingOrder cmoToRemove = cmo(differentStartDate);
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmoToRemove)))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, cmo())))
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
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
        );

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, element(TO_REMOVE_ORDER_ID, cmoToRemove)))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove);

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", updatedHearings,
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, cmo()),
                element(TO_REMOVE_ORDER_ID, cmo(differentStartDate))
            ))
        );
    }

    @Test
    void shouldRemoveCaseManagementOrderWhenOtherCMOisPresent() {
        Element<HearingOrder> cmoToRemove = element(TO_REMOVE_ORDER_ID, cmo());

        Element<HearingBooking> hearingToUpdate = element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID));
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(
                cmoToRemove,
                element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo())
            ))
            .hiddenCaseManagementOrders(null)
            .hearingDetails(newArrayList(
                hearingToUpdate,
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        List<Element<HearingBooking>> updatedHearings = List.of(
            element(HEARING_ID, hearing(null)),
            element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
        );

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, cmoToRemove)).thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove.getValue());

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", updatedHearings,
            "hiddenCaseManagementOrders", List.of(element(TO_REMOVE_ORDER_ID, cmoWithRemovalReason())),
            "sealedCMOs", List.of(element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo()))
        );

        assertThat(caseDetailsMap).isEqualTo(expectedData);
    }

    @Test
    void shouldRemoveHearingAssociationWithARemovedCaseManagementOrder() {
        Element<HearingOrder> cmoToRemove = element(TO_REMOVE_ORDER_ID, cmo());
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(cmoToRemove))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, cmo())))
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

        when(updateCMOHearing.removeHearingLinkedToCMO(caseData, cmoToRemove))
            .thenReturn(updatedHearings);

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove.getValue());

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", updatedHearings,
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, cmo()),
                element(TO_REMOVE_ORDER_ID, cmoWithRemovalReason())
            )
        ));
    }

    @Test
    void shouldThrowExceptionIfOrderNotFound() {
        HearingOrder emptyCaseManagementOrder = cmo();

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
    void shouldPopulateCaseFieldsFromRemovedCMOAndHearingLinkedByCMOId() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        HearingOrder removedOrder = cmo(orderDocument);

        HearingBooking hearingToBeUnlinked = hearing(TO_REMOVE_ORDER_ID);

        Element<HearingBooking> hearing = element(HEARING_ID, hearing(ANOTHER_HEARING_ID));
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                hearing, element(UUID.randomUUID(), hearingToBeUnlinked)
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        when(updateCMOHearing.getHearingToUnlink(caseData, TO_REMOVE_ORDER_ID, removedOrder))
            .thenReturn(hearing.getValue());

        underTest.populateCaseFields(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved",
                "orderTitleToBeRemoved",
                "hearingToUnlink",
                "showRemoveCMOFieldsFlag")
            .containsExactly(orderDocument,
                "Sealed case management order",
                hearingToBeUnlinked.toLabel(),
                YES.getValue());
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedCMOAndHearingLinkedByLabel() {
        LocalDateTime startDate = HEARING_START_DATE.plusDays(3);
        DocumentReference orderDocument = DocumentReference.builder().build();
        HearingOrder removedOrder = cmo(orderDocument, startDate);
        HearingBooking hearingToBeUnlinked = hearing(UUID.randomUUID(), startDate);

        HearingBooking hearing = hearing();
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing),
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
                "showRemoveCMOFieldsFlag")
            .containsExactly(orderDocument,
                "Sealed case management order",
                hearingToBeUnlinked.toLabel(),
                YES.getValue());
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

    private HearingOrder cmoWithRemovalReason() {
        return cmo(null, null, REASON);
    }

    private HearingOrder cmo() {
        return cmo(null, null, null);
    }

    private HearingOrder cmo(DocumentReference order) {
        return cmo(order, null, null);
    }

    private HearingOrder cmo(LocalDateTime startDate) {
        return cmo(null, startDate, REASON);
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
}
