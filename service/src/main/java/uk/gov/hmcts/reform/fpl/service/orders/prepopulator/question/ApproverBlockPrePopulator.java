package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Component
public class ApproverBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private static final String CASE_FIELD_KEY = "judgeAndLegalAdvisor";

    @Override
    public OrderQuestionBlock accept() {
        return APPROVER;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Map<String, Object> judgeMap = Map.of();
        if (caseData.allocatedJudgeExists()) {
            judgeMap = Map.of(CASE_FIELD_KEY, JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel(buildAllocatedJudgeLabel(caseData.getAllocatedJudge()))
                .build());
        }
        return judgeMap;
    }
}
