package uk.gov.hmcts.reform.fpl.selectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChildrenSmartSelector {

    private final ChildSelectionUtils childSelectionUtils;
    private final ChildrenService childrenService;

    public List<Element<Child>> getSelectedChildren(CaseData caseData) {
        boolean onlyOneChildCanBeSelected = childSelectionUtils.canOnlyOneChildBeSelected(caseData);
        if (onlyOneChildCanBeSelected) {
            return childSelectionUtils.getSelectedChildFromSingleSelectionComponent(caseData);
        } else {
            return childrenService.getSelectedChildren(caseData);
        }
    }

}
