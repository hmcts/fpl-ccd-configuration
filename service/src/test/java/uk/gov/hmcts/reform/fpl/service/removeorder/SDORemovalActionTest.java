package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class SDORemovalActionTest {
    private static final String REASON = "Reason";
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final SDORemovalAction underTest = new SDORemovalAction();

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
            .reasonToRemoveOrder(REASON)
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedings(noticeOfProceedings)
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of(
                "state", CASE_MANAGEMENT,
                "standardDirectionOrder", standardDirectionOrder,
                "noticeOfProceedingsBundle", noticeOfProceedings))
            .build());

        underTest.remove(caseData, caseDetailsMap, SDO_ID, standardDirectionOrder);

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        assertThat(caseDetailsMap)
            .extracting("hiddenStandardDirectionOrder", "state")
            .containsExactly(expectedSDO, GATEKEEPING);

        assertThat(caseDetailsMap).doesNotContainKey("standardDirectionOrder");
        assertThat(caseDetailsMap).doesNotContainKey("noticeOfProceedings");
        assertThat(caseDetailsMap).doesNotContainKey("showRemoveSDOWarningFlag");
    }
}
