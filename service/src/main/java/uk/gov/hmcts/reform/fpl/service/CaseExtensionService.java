package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionService {

    private final ChildrenService childrenService;
    private final OptionCountBuilder optionCountBuilder;
    private final ChildrenSmartSelector childrenSmartSelector;

    public LocalDate getCaseCompletionDate(CaseData caseData) {
        if (EIGHT_WEEK_EXTENSION.equals(caseData.getCaseExtensionTimeList())) {
            if (EIGHT_WEEK_EXTENSION.equals(caseData.getCaseExtensionTimeConfirmationList())) {
                return getCaseCompletionDateFor8WeekExtension(caseData);
            }
            return caseData.getEightWeeksExtensionDateOther();
        }
        if(caseData.getExtensionDateOther() != null){
            return caseData.getExtensionDateOther();
        }
        return caseData.getDefaultCompletionDate();
    }

    public LocalDate getCaseCompletionDateFor8WeekExtension(CaseData caseData) {
        return getCaseShouldBeCompletedByDate(caseData).plusWeeks(8);
    }

    public LocalDate getCaseShouldBeCompletedByDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseCompletionDate()).orElse(caseData.getDateSubmitted().plusWeeks(26));
    }


    public String buildChildCaseCompletionDateLabel(CaseData caseData) {
        List<Child> children = caseData.getChildren1().stream().map(Element::getValue).collect(toList());

        StringBuilder sb = new StringBuilder();

        for(int i=0; i < children.size(); i++){
            ChildParty childParty = children.get(i).getParty();
            LocalDate childCaseCompletionDate = this.getCaseCompletionDate(caseData);
            sb.append(String.format("Child %d: %s: %s", i+1, childParty.getFullName(), childCaseCompletionDate))
                .append("\n");
        }
        return sb.toString();
    }

    public Map<String, Object> prePopulateFields(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren1();
        final Selector childSelector = newSelector(children.size());

        return Map.of(
            "childSelectorForExtension", childSelector,
            "children_label", childrenService.getChildrenLabel(children, false),
            "childCaseCompletionDateLabel", this.buildChildCaseCompletionDateLabel(caseData),
            "optionCount", optionCountBuilder.generateCode(children),
            "shouldBeCompletedByDate", this.getCaseShouldBeCompletedByDate(caseData)
        );
    }

    public List<String> validateChildSelector(CaseData caseData) {
        String orderAppliesToAllChildren = caseData.getExtensionForAllChildren();
        Selector childSelector = caseData.getChildSelectorForExtension(); // update this to use the new field for extend timeline

        if (NO.getValue().equals(orderAppliesToAllChildren) && childSelector.getSelected().isEmpty()) {
            return List.of("Select the children requiring an extension");
        }

        return Collections.emptyList();
    }

    public Map<String, String> getSelectedChildren(CaseData caseData) {
        List<Integer> selected = caseData.getChildSelectorForExtension().getSelected();
        List<Child> children = ElementUtils.unwrapElements(caseData.getChildren1());
        Map<String, String> selectedChildren = new HashMap<>();
        selected.forEach(value -> {
                selectedChildren.put(
                        String.join("", "childSelected", value.toString()),
                        "Yes");

                selectedChildren.put(
                        String.join("", "childName", value.toString()),
                        children.get(value).getParty().getFullName());
            }
        );

        return selectedChildren;
    }
}
