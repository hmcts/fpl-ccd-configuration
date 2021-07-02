package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerAboutToStartTest extends AbstractCallbackTest {

    private final Judge allocatedJudge = Judge.builder()
        .judgeTitle(HIS_HONOUR_JUDGE)
        .judgeLastName("Hastings")
        .build();

    AddGatekeepingOrderControllerAboutToStartTest() {
        super("add-gatekeeping-order");
    }

    @Test
    void shouldSetUploadRelatedFieldsWhenUploadedDraftExists() {
        final DocumentReference order = testDocumentReference();

        final CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .gatekeepingOrderRouter(UPLOAD)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderDoc(order)
                .build())
            .build();

        final CaseData updatedCase = extractCaseData(postAboutToStartEvent(caseData));

        final GatekeepingOrderEventData actualEventData = updatedCase.getGatekeepingOrderEventData();

        final GatekeepingOrderEventData expectedEventData = GatekeepingOrderEventData.builder()
            .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
                .build())
            .useUploadRoute(YES)
            .currentSDO(order)
            .build();

        assertThat(actualEventData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldSetGeneratedOrderRelatedFieldsWhenUploadedDraftExists() {
        final DocumentReference order = testDocumentReference();

        final CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .gatekeepingOrderRouter(SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderDoc(order)
                .build())
            .build();

        final CaseData updatedCase = extractCaseData(postAboutToStartEvent(caseData));

        final GatekeepingOrderEventData actualEventData = updatedCase.getGatekeepingOrderEventData();

        final GatekeepingOrderEventData expectedEventData = GatekeepingOrderEventData.builder()
            .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
                .build())
            .useServiceRoute(YES)
            .build();

        assertThat(actualEventData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldSetAllocatedJudgeLabelOnIssuingJudgeWhenAllocatedJudgeOnCase() {
        final CaseData caseData = CaseData.builder()
            .allocatedJudge(allocatedJudge)
            .build();

        final CaseData updatedCase = extractCaseData(postAboutToStartEvent(caseData));

        final GatekeepingOrderEventData actualEventData = updatedCase.getGatekeepingOrderEventData();

        final GatekeepingOrderEventData expectedEventData = GatekeepingOrderEventData.builder()
            .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
                .build())
            .build();

        assertThat(actualEventData).isEqualTo(expectedEventData);
    }

    @Test
    void shouldNotSetAllocatedJudgeLabelNorHearingDetails() {
        final CaseData caseData = CaseData.builder().build();

        final CaseData updatedCase = extractCaseData(postAboutToStartEvent(caseData));

        final GatekeepingOrderEventData actualEventData = updatedCase.getGatekeepingOrderEventData();

        final GatekeepingOrderEventData expectedEventData = GatekeepingOrderEventData.builder()
            .build();

        assertThat(actualEventData).isEqualTo(expectedEventData);
    }
}
