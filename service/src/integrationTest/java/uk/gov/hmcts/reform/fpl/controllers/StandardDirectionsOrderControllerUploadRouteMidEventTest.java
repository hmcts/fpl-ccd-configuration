package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerUploadRouteMidEventTest extends AbstractControllerTest {

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
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = extractCaseData(response).getStandardDirectionOrder();

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
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor("some label");

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

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .build();
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor(String allocatedJudgeLabel) {
        return buildJudgeAndLegalAdvisor().toBuilder()
            .allocatedJudgeLabel(allocatedJudgeLabel)
            .build();
    }
}
