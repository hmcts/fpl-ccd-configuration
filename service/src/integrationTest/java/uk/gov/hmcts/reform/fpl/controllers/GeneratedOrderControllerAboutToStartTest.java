package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;

@ActiveProfiles("integration-test")
@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class GeneratedOrderControllerAboutToStartTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    GeneratedOrderControllerAboutToStartTest() {
        super("create-order");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void aboutToStartShouldSetDateOfIssueAsTodayByDefault() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("familyManCaseNumber", "123"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData().get("dateOfIssue")).isEqualTo(dateNow().toString());
    }

    @Test
    void aboutToStartShouldSetAssignJudgeLabelWhenAllocatedJudgeIsPopulated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Richards")
                    .build()
            )).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: His Honour Judge Richards");
    }

    @Test
    void aboutToStartShouldNotSetAssignedJudgeLabelIfAllocatedJudgeNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build()
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
    }
}
