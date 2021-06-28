package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.List;
import java.util.Map;

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
        final Selector othersSelector = newSelector(caseData.getAllOthers().size());
        return Map.of(
            "othersSelector", othersSelector,
            "others_label", othersService.getOthersLabel(caseData.getAllOthers())
        );
    }
}
