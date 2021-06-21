package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerAboutToStartTest extends AbstractCallbackTest {
    AddGatekeepingOrderControllerAboutToStartTest() {
        super("add-gatekeeping-order");
    }

    @Test
    void shouldSetAllocatedJudgeLabelOnIssuingJudgeWhenAllocatedJudgeOnCase() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder().judgeTitle(HIS_HONOUR_JUDGE).judgeLastName("Hastings").build())
            .build();

        JudgeAndLegalAdvisor expectedJudge = JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
            .build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge())
            .isEqualTo(expectedJudge);
    }

    @Test
    void shouldNotSetAllocatedJudgeLabelOnIssuingJudgeWhenNoAllocatedJudgeOnCase() {
        CaseData caseData = CaseData.builder().build();

        CaseData responseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge())
            .isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }
}
