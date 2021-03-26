package uk.gov.hmcts.reform.fpl.service.orders;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;

public interface QuestionBlockOrderValidator {

    OrderQuestionBlock accept();

    List<String> validate(CaseDetails caseData);
}
