package uk.gov.hmcts.reform.fpl.service.orders.validator;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;

public interface QuestionBlockOrderValidator {

    OrderQuestionBlock accept();

    List<String> validate(CaseData caseData);
}
