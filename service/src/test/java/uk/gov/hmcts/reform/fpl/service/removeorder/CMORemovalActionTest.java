package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.UnexpectedNumberOfCMOsRemovedException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CMORemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private static final UUID CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final UUID HEARING_ID = UUID.randomUUID();
    private static final UUID ANOTHER_HEARING_ID = UUID.randomUUID();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.now();

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
    void shouldNotRemoveOrderWhenUniqueHearingAssociationCannotBeDetermined() {
        CaseManagementOrder cmoToRemove = cmo(HEARING_START_DATE);
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmoToRemove)))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, cmo())))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThatThrownBy(() -> underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove))
            .usingRecursiveComparison()
            .isEqualTo(unexpectedNumberOfCMOsRemovedException(TO_REMOVE_ORDER_ID, 2));
    }

    @Test
    void shouldRemoveOrderWhenNoMatchingIDButMatchingHearingLabel() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        CaseManagementOrder cmoToRemove = cmo(differentStartDate);
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

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmoToRemove);

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(null, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ),
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, cmo()),
                element(TO_REMOVE_ORDER_ID, cmo(differentStartDate, REASON))
            ))
        );
    }

    @Test
    void shouldRemoveCaseManagementOrderWhenOtherCMOisPresent() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(
                element(TO_REMOVE_ORDER_ID, cmo()),
                element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo())
            ))
            .hiddenCaseManagementOrders(null)
            .hearingDetails(newArrayList(
                element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmo());

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(null)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ),
            "hiddenCaseManagementOrders", List.of(element(TO_REMOVE_ORDER_ID, cmoWithRemovalReason())),
            "sealedCMOs", List.of(element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo()))
        );

        assertThat(caseDetailsMap).isEqualTo(expectedData);
    }

    @Test
    void shouldRemoveHearingAssociationWithARemovedCaseManagementOrder() {
        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmo())))
            .hiddenCaseManagementOrders(newArrayList(element(ALREADY_REMOVED_ORDER_ID, cmo())))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(TO_REMOVE_ORDER_ID))))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of())
            .build());

        underTest.remove(caseData, caseDetailsMap, TO_REMOVE_ORDER_ID, cmo());

        assertThat(caseDetailsMap).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(null))
            ),
            "hiddenCaseManagementOrders", List.of(
                element(ALREADY_REMOVED_ORDER_ID, cmo()),
                element(TO_REMOVE_ORDER_ID, cmoWithRemovalReason())
            )
        ));
    }

    @Test
    void shouldThrowExceptionIfOrderNotFound() {
        CaseManagementOrder emptyCaseManagementOrder = cmo();

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
        CaseManagementOrder removedOrder = cmo(orderDocument);

        HearingBooking hearingToBeUnlinked = hearing(TO_REMOVE_ORDER_ID);

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(ANOTHER_HEARING_ID)),
                element(UUID.randomUUID(), hearingToBeUnlinked)
            ))
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
    void shouldPopulateCaseFieldsFromRemovedCMOAndHearingLinkedByLabel() {
        LocalDateTime startDate = HEARING_START_DATE.plusDays(3);
        DocumentReference orderDocument = DocumentReference.builder().build();
        CaseManagementOrder removedOrder = cmo(orderDocument, startDate);
        HearingBooking hearingToBeUnlinked = hearing(UUID.randomUUID(), startDate);

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing()),
                element(UUID.randomUUID(), hearingToBeUnlinked)
            ))
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
    void shouldThrowAnExceptionIfUniqueHearingNotFound() {
        CaseManagementOrder removedOrder = cmo(HEARING_START_DATE);

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .sealedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, removedOrder)))
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
    void shouldRemoveDraftCaseManagementOrderAndUnlinkHearing() {
        Element<CaseManagementOrder> orderToBeRemoved = element(TO_REMOVE_ORDER_ID, cmo());

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(
                orderToBeRemoved,
                element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo())
            ))
            .hearingDetails(newArrayList(
                element(HEARING_ID, hearing(TO_REMOVE_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        underTest.removeDraftCaseManagementOrder(caseData, caseDetails, orderToBeRemoved);

        Map<String, List<?>> expectedData = Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(null)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ),
            "draftUploadedCMOs", List.of(element(ANOTHER_CASE_MANAGEMENT_ORDER_ID, cmo()))
        );

        assertThat(caseDetails.getData()).isEqualTo(expectedData);
    }

    @Test
    void shouldNotRemoveHearingWhenCannotSafelyDetermineUniqueHearingToBeRemoved() {
        Element<CaseManagementOrder> cmoToRemove = element(TO_REMOVE_ORDER_ID, CaseManagementOrder.builder().build());

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .draftUploadedCMOs(newArrayList(cmoToRemove))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        assertThatThrownBy(() -> underTest.removeDraftCaseManagementOrder(caseData, caseDetails, cmoToRemove))
            .usingRecursiveComparison()
            .isEqualTo(unexpectedNumberOfCMOsRemovedException(TO_REMOVE_ORDER_ID, 2));
    }

    @Test
    void shouldRemoveDraftOrderWhenNoMatchingIDButMatchingHearingLabel() {
        LocalDateTime differentStartDate = HEARING_START_DATE.plusDays(3);
        Element<CaseManagementOrder> cmoToRemove = element(TO_REMOVE_ORDER_ID, cmo(differentStartDate));
        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(cmoToRemove))
            .hearingDetails(List.of(
                element(HEARING_ID, hearing(CASE_MANAGEMENT_ORDER_ID, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        underTest.removeDraftCaseManagementOrder(caseData, caseDetails, cmoToRemove);

        assertThat(caseDetails.getData()).isEqualTo(Map.of(
            "hearingDetails", List.of(
                element(HEARING_ID, hearing(null, differentStartDate)),
                element(ANOTHER_HEARING_ID, hearing(ANOTHER_CASE_MANAGEMENT_ORDER_ID))
            ))
        );
    }

    @Test
    void shouldThrowAnExceptionIfDraftOrderToBeRemovedIsNotFound() {
        Element<CaseManagementOrder> removedOrder = element(ALREADY_REMOVED_ORDER_ID, cmo());

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .draftUploadedCMOs(newArrayList(element(TO_REMOVE_ORDER_ID, cmo())))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

        assertThatThrownBy(() -> underTest.removeDraftCaseManagementOrder(caseData, caseDetails, removedOrder))
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

    private CaseManagementOrder cmoWithRemovalReason() {
        return cmo(null, null, REASON);
    }

    private CaseManagementOrder cmo() {
        return cmo(null, null, null);
    }

    private CaseManagementOrder cmo(DocumentReference order) {
        return cmo(order, null, null);
    }

    private CaseManagementOrder cmo(LocalDateTime startDate) {
        return cmo(null, startDate, null);
    }

    private CaseManagementOrder cmo(DocumentReference order, LocalDateTime startDate) {
        return cmo(order, startDate, null);
    }

    private CaseManagementOrder cmo(LocalDateTime startDate, String removalReason) {
        return cmo(null, startDate, removalReason);
    }

    private CaseManagementOrder cmo(DocumentReference order, LocalDateTime startDate, String removalReason) {
        return CaseManagementOrder.builder()
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
