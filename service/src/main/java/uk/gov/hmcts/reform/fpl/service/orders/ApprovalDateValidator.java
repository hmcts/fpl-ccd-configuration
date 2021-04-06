package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;

@Component
public class ApprovalDateValidator implements QuestionBlockOrderValidator {

    @Override
    public OrderQuestionBlock accept() {
        return APPROVAL_DATE;
    }

    @Override
    public List<String> validate(CaseDetails caseDetails) {
        // test
        return List.of();
    }
}
