package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.RespondentsRefusedFormatter;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentsRefusedBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final RespondentsRefusedFormatter respondentsRefusedFormatter;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.RESPONDENTS_REFUSED;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        final Selector respondentsRefusedSelector = newSelector(
            caseData.getAllRespondents().size() + caseData.getAllOthers().size());
        return Map.of(
            "respondentsRefusedSelector", respondentsRefusedSelector,
            "respondentsRefused_label",
            respondentsRefusedFormatter.getRespondentsRefusedLabel(caseData)
        );
    }
}
