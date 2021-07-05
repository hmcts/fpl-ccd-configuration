package uk.gov.hmcts.reform.fpl.selectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChildrenSmartSelector {

    private final ChildrenService childrenService;

    public List<Element<Child>> getSelectedChildren(CaseData caseData) {
        boolean onlyOneChildCanBeSelected = canOnlyOneChildBeSelected(caseData);
        if (onlyOneChildCanBeSelected) {
            return getSelectedChildFromSingleSelectionComponent(caseData);
        } else {
            return childrenService.getSelectedChildren(caseData);
        }
    }

    public static boolean canOnlyOneChildBeSelected(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getManageOrdersEventData)
            .map(ManageOrdersEventData::getOrderTempQuestions)
            .map(OrderTempQuestions::getSelectSingleChild)
            .map("YES"::equalsIgnoreCase)
            .orElse(false);
    }

    public static List<Element<Child>> getSelectedChildFromSingleSelectionComponent(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getManageOrdersEventData)
            .map(ManageOrdersEventData::getWhichChildIsTheOrderFor)
            .map(DynamicList::getValueCode)
            .map(UUID::fromString)
            .map(selectedChildId -> getSelectedChildFromCaseData(caseData, selectedChildId))
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
    }

    private static Element<Child> getSelectedChildFromCaseData(CaseData caseData, UUID selectedChildId) {
        return caseData.getAllChildren().stream()
            .filter(c -> selectedChildId.equals(c.getId()))
            .findFirst()
            .orElseThrow();
    }

}
