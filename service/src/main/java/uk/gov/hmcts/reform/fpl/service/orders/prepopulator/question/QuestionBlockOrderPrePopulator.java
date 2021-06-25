package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

public interface QuestionBlockOrderPrePopulator {

    OrderQuestionBlock accept();

    Map<String,Object> prePopulate(CaseData caseData);

}
