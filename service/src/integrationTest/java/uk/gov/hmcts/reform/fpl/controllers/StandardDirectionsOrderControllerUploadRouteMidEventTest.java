package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerUploadRouteMidEventTest extends AbstractCallbackTest {

    private static final DocumentReference DOCUMENT = testDocumentReference("prepared.pdf");

    StandardDirectionsOrderControllerUploadRouteMidEventTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldAddPreparedSDOToConstructedStandardDirectionOrder() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(DOCUMENT)
            .build();

        CallbackRequest request = toCallBackRequest(asCaseDetails(caseData), asCaseDetails(CaseData.builder().build()));
        CaseData responseData = extractCaseData(postMidEvent(request, "upload-route"));

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = responseData.getStandardDirectionOrder();

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }

    @Test
    void shouldAddAlreadyUploadedDocumentToConstructedStandardDirectionOrder() {
        CaseData caseDataBefore = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderDoc(DOCUMENT).build())
            .build();

        CallbackRequest request = toCallBackRequest(
            asCaseDetails(CaseData.builder().build()),
            asCaseDetails(caseDataBefore)
        );
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = extractCaseData(response).getStandardDirectionOrder();

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }

    @Test
    void shouldAppendJudgeAndLegalAdvisorToSDO() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor().toBuilder()
            .allocatedJudgeLabel("some label")
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderDoc(DOCUMENT).build())
            .build();

        CallbackRequest request = toCallBackRequest(
            asCaseDetails(CaseData.builder()
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .build()),
            asCaseDetails(caseDataBefore)
        );

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");
        StandardDirectionOrder builtOrder = extractCaseData(response).getStandardDirectionOrder();

        JudgeAndLegalAdvisor actualJudgeAndLegalAdvisor = builtOrder.getJudgeAndLegalAdvisor();

        assertThat(actualJudgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
        assertThat(actualJudgeAndLegalAdvisor).isEqualTo(buildJudgeAndLegalAdvisor());
    }

    @Test
    void shouldPopulateShowNoticeOfProceedingsWhenInGatekeepingState() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(DOCUMENT)
            .state(State.GATEKEEPING)
            .build();

        CallbackRequest request = toCallBackRequest(asCaseDetails(caseData), asCaseDetails(CaseData.builder().build()));
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        assertThat(response.getData().get("showNoticeOfProceedings")).isEqualTo("YES");
    }

    @Test
    void shouldPopulateShowNoticeOfProceedingsWhenNotInGatekeepingState() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(DOCUMENT)
            .state(State.CASE_MANAGEMENT)
            .build();

        CallbackRequest request = toCallBackRequest(asCaseDetails(caseData), asCaseDetails(CaseData.builder().build()));
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        assertThat(response.getData().get("showNoticeOfProceedings")).isEqualTo("NO");
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }
}
