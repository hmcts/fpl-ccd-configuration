package uk.gov.hmcts.reform.fpl.selectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChildrenSmartSelector {

    private final ChildSelectionUtils childSelectionUtils;
    private final ChildrenService childrenService;
    private final PlacementService placementService;

    public List<Element<Child>> getSelectedChildren(CaseData caseData) {
        List<Element<Child>> childrenToReturn;

        boolean isChildSelectedByPlacementApplication =
            childSelectionUtils.isChildSelectedByPlacementApplication(caseData);
        if (isChildSelectedByPlacementApplication) {
            UUID placementId =
                caseData.getManageOrdersEventData().getManageOrdersChildPlacementApplication().getValueCodeAsUUID();
            childrenToReturn = List.of(placementService.getChildByPlacementId(caseData, placementId));
        } else {
            boolean onlyOneChildCanBeSelected = childSelectionUtils.canOnlyOneChildBeSelected(caseData);
            if (onlyOneChildCanBeSelected) {
                childrenToReturn = childSelectionUtils.getSelectedChildFromSingleSelectionComponent(caseData);
            } else {
                childrenToReturn = childrenService.getSelectedChildren(caseData);
            }
        }

        return childrenToReturn;
    }

}
