package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Iterables.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_OTHERS;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WhichOthersBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final OthersService othersService;

    @Override
    public OrderQuestionBlock accept() {
        return WHICH_OTHERS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        OrderTempQuestions orderTempQuestions = caseData.getManageOrdersEventData().getOrderTempQuestions();
        final Selector othersSelector = newSelector(caseData.getAllOthers().size());

        if (isEmpty(caseData.getAllOthers())) {
            data.put("orderTempQuestions", orderTempQuestions.toBuilder().whichOthers("NO").build());
        }

        data.put("othersSelector", othersSelector);
        data.put("others_label", othersService.getOthersLabel(caseData.getAllOthers()));

        return data;
    }
}
