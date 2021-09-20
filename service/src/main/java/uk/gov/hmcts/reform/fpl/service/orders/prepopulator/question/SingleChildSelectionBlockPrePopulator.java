package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SELECT_SINGLE_CHILD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Component
public class SingleChildSelectionBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    @Override
    public OrderQuestionBlock accept() {
        return SELECT_SINGLE_CHILD;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        return Map.of("whichChildIsTheOrderFor", asDynamicList(caseData.getAllChildren(), Child::asLabel));
    }

}
