package uk.gov.hmcts.reform.fpl.updaters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector.canOnlyOneChildBeSelected;
import static uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector.getSelectedChildFromSingleSelectionComponent;

@Component
@RequiredArgsConstructor
public class ChildrenSmartFinalOrderUpdater {

    private final ChildrenService childrenService;

    public List<Element<Child>> updateFinalOrderIssued(CaseData caseData) {
        List<Element<Child>> selectedChildren = getSelectedChildrenForIssuingFinalOrder(caseData);

        return childrenService.updateFinalOrderIssued(
            caseData.getManageOrdersEventData().getManageOrdersType().getTitle(),
            caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(),
            selectedChildren);
    }

    private List<Element<Child>> getSelectedChildrenForIssuingFinalOrder(CaseData caseData) {
        final List<Element<Child>> selectedChildren;

        boolean onlyOneChildCanBeSelected = canOnlyOneChildBeSelected(caseData);
        if (onlyOneChildCanBeSelected) {
            selectedChildren = getSelectedChildFromSingleSelectionComponent(caseData);
        } else {
            selectedChildren = childrenService.getSelectedChildrenForIssuingFinalOrder(caseData);
        }

        return selectedChildren;
    }

}
