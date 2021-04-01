package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Component
public class ApproverBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return APPROVER;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData, CaseDetails caseDetails) {
        return Map.of("judgeAndLegalAdvisor", Map.of(
            "allocatedJudgeLabel", buildAllocatedJudgeLabel(caseData.getAllocatedJudge())
        ));
    }
}
