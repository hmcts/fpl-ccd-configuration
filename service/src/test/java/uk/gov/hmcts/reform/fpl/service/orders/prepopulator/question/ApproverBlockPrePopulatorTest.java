package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

class ApproverBlockPrePopulatorTest {

    private static final Judge ALLOCATED_JUDGE = mock(Judge.class);
    private static final String FORMATTED_JUDGE = "formattedJudge";
    private final ApproverBlockPrePopulator underTest = new ApproverBlockPrePopulator();

    @Test
    public void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.APPROVER);
    }

    @Test
    public void prePopulate() {

        CaseData caseData = CaseData.builder()
            .allocatedJudge(ALLOCATED_JUDGE)
            .build();

        try (MockedStatic<JudgeAndLegalAdvisorHelper> helper =
                 Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {
            helper.when(() -> JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel(ALLOCATED_JUDGE)).thenReturn(
                FORMATTED_JUDGE);

            assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of(
                "judgeAndLegalAdvisor", Map.of(
                    "allocatedJudgeLabel", buildAllocatedJudgeLabel(ALLOCATED_JUDGE)
                )
            ));
        }


    }
}
