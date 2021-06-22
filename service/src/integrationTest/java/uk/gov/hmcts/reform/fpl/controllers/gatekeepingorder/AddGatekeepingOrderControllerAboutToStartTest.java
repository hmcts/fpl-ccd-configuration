package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
    void shouldSetHearingDateIfHearingPresent() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder()
                .type(HearingType.CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2030, 2, 10, 10, 30, 0))
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getData()).contains(
            entry("gatekeepingOrderHasHearing1", "YES"),
            entry("gatekeepingOrderHasHearing2", "YES"),
            entry("gatekeepingOrderHearingDate1", "10 February 2030, 10:30am"),
            entry("gatekeepingOrderHearingDate2", "10 February 2030, 10:30am")
        );
    }

    @Test
    void shouldNotSetAllocatedJudgeLabelNorHearingDetails() {
        CaseData caseData = CaseData.builder().build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(extractCaseData(callbackResponse).getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge())
            .isEqualTo(JudgeAndLegalAdvisor.builder().build());

        assertThat(callbackResponse.getData()).doesNotContainKeys(
            "gatekeepingOrderHasHearing1",
            "gatekeepingOrderHasHearing2",
            "gatekeepingOrderHearingDate1",
            "gatekeepingOrderHearingDate2");
    }
}
