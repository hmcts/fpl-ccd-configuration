package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class AssignJudgeControllerMidEventTest extends AbstractControllerTest {
    AssignJudgeControllerMidEventTest() {
        super("assign-judge");
    }

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldSetAssignJudgeLabelWhenAllocatedJudgeIsPopulated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Richards")
                    .build()
            )).build();


        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: His Honour Judge Richards");
    }

    @Test
    void shouldNotSetAssignedJudgeLabelIfAllocatedJudgeNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build()
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = caseData.getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
    }
}
