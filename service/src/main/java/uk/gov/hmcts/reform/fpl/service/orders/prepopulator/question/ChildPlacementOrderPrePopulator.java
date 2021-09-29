package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.PlacementService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_PLACEMENT_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Component
@RequiredArgsConstructor
public class ChildPlacementOrderPrePopulator implements QuestionBlockOrderPrePopulator {

    private final PlacementService placementService;

    @Override
    public OrderQuestionBlock accept() {
        return CHILD_PLACEMENT_APPLICATIONS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        List<Element<String>> childPlacementOrders = placementService.getPlacements(caseData);
        return Map.of("manageOrdersChildPlacementApplication", asDynamicList(childPlacementOrders, label -> label));
    }

}
