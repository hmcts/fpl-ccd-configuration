package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WhichChildrenBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final ChildrenService childrenService;

    @Override
    public OrderQuestionBlock accept() {
        return WHICH_CHILDREN;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        final DynamicMultiSelectList childSelectorV2 = childrenService
            .getChildrenMultiSelectList(caseData);
        return Map.of(
            "childSelectorV2", childSelectorV2,
            "children_label", childrenService.getChildrenLabelFromMultiSelectList(childSelectorV2)
        );
    }
}
