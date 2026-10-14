package uk.gov.hmcts.reform.fpl.updaters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;

import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ChildrenSmartFinalOrderUpdater {

    private final ChildSelectionUtils childSelectionUtils;
    private final ChildrenService childrenService;

    public List<Element<Child>> updateFinalOrderIssued(CaseData caseData) {
        if (caseData == null) {
            return Collections.emptyList();
        }
        List<Element<Child>> selectedChildren = getSelectedChildrenForIssuingFinalOrder(caseData);

        return childrenService.updateFinalOrderIssued(
            caseData.getManageOrdersEventData().getManageOrdersType().getTitle(),
            caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(),
            selectedChildren);
    }

    private List<Element<Child>> getSelectedChildrenForIssuingFinalOrder(CaseData caseData) {
        final List<Element<Child>> selectedChildren;

        boolean onlyOneChildCanBeSelected = childSelectionUtils.canOnlyOneChildBeSelected(caseData);
        List<Element<Child>> childList = childrenService.getSelectedChildrenFromMultiSelectList(caseData);
        boolean dynamicMultiSelectListEmpty = childList.isEmpty();

        if (onlyOneChildCanBeSelected) {
            selectedChildren = childSelectionUtils.getSelectedChildFromSingleSelectionComponent(caseData);
        } else if (!dynamicMultiSelectListEmpty) {
            selectedChildren = childrenService.getSelectedChildrenFromMultiSelectList(caseData);
        } else {
            selectedChildren = childrenService.getSelectedChildrenForIssuingFinalOrder(caseData);
        }

        return selectedChildren;
    }

}
