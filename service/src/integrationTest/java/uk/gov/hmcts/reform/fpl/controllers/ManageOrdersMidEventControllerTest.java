package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersMidEventControllerTest extends AbstractCallbackTest {

    ManageOrdersMidEventControllerTest() {
        super("manage-orders");
    }


    @Test
    void section1ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(Order.C32_CARE_ORDER).build())
            .allocatedJudge(Judge.builder()
                .judgeLastName("Judy")
                .judgeTitle(DISTRICT_JUDGE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-1");

        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(callbackResponse.getData().get("orderTempQuestions")).isEqualTo(
            Map.of(
                "approvalDate", "YES",
                "approver", "YES",
                "previewOrder", "YES",
                "furtherDirections", "YES",
                "whichChildren", "YES"
            )
        );

        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(
            JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: District Judge Judy")
                .build()
        );

    }

    @Test
    void section2ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(Order.C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-2");

        CaseData responseCaseData = extractCaseData(callbackResponse);

    }

    @Test
    void section3ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(Order.C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-3");

        CaseData responseCaseData = extractCaseData(callbackResponse);

    }

    @Test
    void sectionReviewShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(Order.C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "review");

        CaseData responseCaseData = extractCaseData(callbackResponse);

    }
}
