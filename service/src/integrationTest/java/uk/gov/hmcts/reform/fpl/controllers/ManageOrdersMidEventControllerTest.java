package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersMidEventControllerTest extends AbstractCallbackTest {


    private static final Long CASE_ID = 12345L;

    ManageOrdersMidEventControllerTest() {
        super("manage-orders");
    }


    @Test
    void section1ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersType(Order.C32_CARE_ORDER)
            .allocatedJudge(Judge.builder()
                .judgeLastName("Judjy")
                .judgeTitle(DISTRICT_JUDGE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-1");

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(callbackResponse.getData().get("orderTempQuestions")).isEqualTo(
            Map.of(
                "approvalDate", "YES",
                "approver", "YES",
                "draftOrder", "YES",
                "furtherDirections", "YES",
                "whichChildren", "YES"
            )
        );

        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(
            JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: District Judge Judjy")
                .build()
        );

    }

    @Test
    void section2ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersType(Order.C32_CARE_ORDER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-2");

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

    }

    @Test
    void section3ShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersType(Order.C32_CARE_ORDER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "section-3");

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

    }

    @Test
    void sectionReviewShouldCorrectlySetAllDetails() {
        final CaseData caseData = CaseData.builder()
            .manageOrdersType(Order.C32_CARE_ORDER)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData), "review");

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

    }

    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDetails buildCaseData() {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of())
            .build();
    }
}
