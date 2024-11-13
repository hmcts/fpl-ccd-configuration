package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class SDORemovalActionTest {
    private static final String REASON = "Reason";
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ANOTHER_SDO_ID = UUID.randomUUID();

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private SDORemovalAction underTest;

    @Test
    void isAcceptedIfStandardDirectionOrder() {
        assertThat(underTest.isAccepted(mock(StandardDirectionOrder.class))).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedSDO() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        StandardDirectionOrder removedOrder = StandardDirectionOrder.builder()
            .orderDoc(orderDocument)
            .build();

        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(removedOrder)
            .noticeOfProceedingsBundle(List.of(element(DocumentBundle.builder().build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        underTest.populateCaseFields(caseData, caseDetailsMap, SDO_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved", "orderTitleToBeRemoved", "showRemoveSDOWarningFlag")
            .containsExactly(orderDocument, "Gatekeeping order", YES.getValue());
    }

    @Test
    void shouldPopulateCaseFieldsFromRemovedUDO() {
        DocumentReference orderDocument = DocumentReference.builder().build();
        StandardDirectionOrder removedOrder = StandardDirectionOrder.builder()
            .orderDoc(orderDocument)
            .build();

        CaseData caseData = CaseData.builder()
            .urgentDirectionsOrder(removedOrder)
            .noticeOfProceedingsBundle(List.of(element(DocumentBundle.builder().build())))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(Map.of()).build());

        underTest.populateCaseFields(caseData, caseDetailsMap, SDO_ID, removedOrder);

        assertThat(caseDetailsMap)
            .extracting("orderToBeRemoved", "orderTitleToBeRemoved", "showRemoveSDOWarningFlag")
            .containsExactly(orderDocument, "Gatekeeping order", YES.getValue());
    }

    @Test
    void shouldRemoveSDONoticeOfProceedingsAndSetStateToGatekeepingWhenRemovingSealedSDO() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder().build();

        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveOrder(REASON)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedings(noticeOfProceedings)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of(
                "state", CASE_MANAGEMENT,
                "standardDirectionOrder", standardDirectionOrder,
                "noticeOfProceedingsBundle", noticeOfProceedings))
            .build());

        when(identityService.generateId()).thenReturn(SDO_ID);
        underTest.remove(caseData, caseDetailsMap, SDO_ID, standardDirectionOrder);

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        assertThat(caseDetailsMap)
            .extracting("hiddenStandardDirectionOrders", "state")
            .containsExactly(List.of(element(SDO_ID, expectedSDO)), GATEKEEPING);

        assertThat(caseDetailsMap).doesNotContainKeys(
            "standardDirectionOrder", "noticeOfProceedings","showRemoveSDOWarningFlag");
    }

    @Test
    void shouldRemoveUDONoticeOfProceedingsAndSetStateToGatekeepingWhenRemovingSealedUDO() {
        StandardDirectionOrder urgentDirectionsOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder().build();

        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveOrder(REASON)
                .build())
            .urgentDirectionsOrder(urgentDirectionsOrder)
            .noticeOfProceedings(noticeOfProceedings)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of(
                "state", CASE_MANAGEMENT,
                "urgentDirectionsOrder", urgentDirectionsOrder,
                "noticeOfProceedingsBundle", noticeOfProceedings))
            .build());

        when(identityService.generateId()).thenReturn(SDO_ID);
        underTest.remove(caseData, caseDetailsMap, SDO_ID, urgentDirectionsOrder);

        StandardDirectionOrder expectedUDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        assertThat(caseDetailsMap)
            .extracting("hiddenUrgentDirectionOrders", "state")
            .containsExactly(List.of(element(SDO_ID, expectedUDO)), GATEKEEPING);

        assertThat(caseDetailsMap).doesNotContainKeys(
            "urgentDirectionsOrder", "noticeOfProceedings","showRemoveSDOWarningFlag");
    }

    @Test
    void shouldRemoveAllDirectionsWhenRemovingSDO() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder().build();

        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveOrder(REASON)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedings(noticeOfProceedings)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of(
                "allParties", List.of(),
                "allPartiesCustom", List.of(),
                "localAuthorityDirections", List.of(),
                "localAuthorityDirectionsCustom", List.of(),
                "courtDirections", List.of(),
                "courtDirectionsCustom", List.of(),
                "cafcassDirections", List.of(),
                "otherPartiesDirections", List.of(),
                "otherPartiesDirectionsCustom", List.of(),
                "respondentDirections", List.of()))
            .build());

        when(identityService.generateId()).thenReturn(SDO_ID);
        underTest.remove(caseData, caseDetailsMap, SDO_ID, standardDirectionOrder);

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        assertThat(caseDetailsMap)
            .extracting("hiddenStandardDirectionOrders", "state")
            .containsExactly(List.of(element(SDO_ID, expectedSDO)), GATEKEEPING);

        assertThat(caseDetailsMap).doesNotContainKeys(
            "allParties", "allPartiesCustom","localAuthorityDirections", "localAuthorityDirectionsCustom",
            "courtDirections", "courtDirectionsCustom", "cafcassDirections", "otherPartiesDirections",
            "otherPartiesDirectionsCustom", "respondentDirections");
    }

    @Test
    void shouldPersistPreviouslyRemovedSDO() {
        StandardDirectionOrder previouslyRemovedSDO = StandardDirectionOrder.builder().build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        NoticeOfProceedings noticeOfProceedings = NoticeOfProceedings.builder().build();

        List<Element<StandardDirectionOrder>> hiddenSDOs = new ArrayList<>();
        hiddenSDOs.add(element(SDO_ID, previouslyRemovedSDO));

        CaseData caseData = CaseData.builder()
            .removalToolData(RemovalToolData.builder()
                .reasonToRemoveOrder(REASON)
                .hiddenStandardDirectionOrders(hiddenSDOs)
                .build())
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedings(noticeOfProceedings)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of(
                "state", CASE_MANAGEMENT,
                "standardDirectionOrder", standardDirectionOrder,
                "noticeOfProceedingsBundle", noticeOfProceedings))
            .build());

        when(identityService.generateId()).thenReturn(ANOTHER_SDO_ID);

        underTest.remove(caseData, caseDetailsMap, SDO_ID, standardDirectionOrder);

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        assertThat(caseDetailsMap)
            .extracting("hiddenStandardDirectionOrders", "state")
            .containsExactly(List.of(
                element(SDO_ID, previouslyRemovedSDO),
                element(ANOTHER_SDO_ID, expectedSDO)), GATEKEEPING);

        assertThat(caseDetailsMap).doesNotContainKeys(
            "standardDirectionOrder", "noticeOfProceedings","showRemoveSDOWarningFlag");
    }
}
