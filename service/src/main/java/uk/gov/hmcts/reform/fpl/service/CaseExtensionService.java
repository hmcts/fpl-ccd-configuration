package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionService {

    private final ChildrenService childrenService;
    private final OptionCountBuilder optionCountBuilder;
    private final ChildrenSmartSelector childrenSmartSelector;
    private final ValidateGroupService validateGroupService;

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
        List<Child> children = ElementUtils.unwrapElements(caseData.getChildren1());

        StringBuilder sb = new StringBuilder();

        for(int i=0; i < children.size(); i++){
            ChildParty childParty = children.get(i).getParty();
            String childCaseCompletionDate = formatLocalDateToString(
                    Optional.ofNullable(childParty.getCompletionDate())
                    .orElseGet(caseData::getDefaultCompletionDate),
                    DATE);
            sb.append(String.format("Child %d: %s: %s", i+1, childParty.getFullName(), childCaseCompletionDate))
                .append(System.lineSeparator());
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
        String orderAppliesToAllChildren = caseData.getChildExtensionEventData().getExtensionForAllChildren();
        Selector childSelector = caseData.getChildExtensionEventData().getChildSelectorForExtension(); // update this to use the new field for extend timeline

        if (NO.getValue().equals(orderAppliesToAllChildren) && childSelector.getSelected().isEmpty()) {
            return List.of("Select the children requiring an extension");
        }

        return Collections.emptyList();
    }

    public Map<String, Object> getSelectedChildren(CaseData caseData) {
        List<Integer> selected = caseData.getChildExtensionEventData().getChildSelectorForExtension().getSelected();
        List<Element<Child>> children = caseData.getChildren1();
        Map<String, Object> selectedChildren = new HashMap<>();

        selected.forEach(value -> setChildDetails(children, selectedChildren, value));
        return selectedChildren;
    }

    private void setChildDetails(List<Element<Child>> children, Map<String, Object> selectedChildren, int value) {
        selectedChildren.put(
            String.join("", "childSelected", String.valueOf(value)),
            YES.getValue());

        selectedChildren.put(
            String.join("", "childExtension", String.valueOf(value)),
            ChildExtension.builder()
                .label(children.get(value).getValue().getParty().getFullName())
                .id(children.get(value).getId())
                .build());
    }

    public Map<String, Object> getAllChildren(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren1();
        Map<String, Object> selectedChildren = new HashMap<>();
        for(int i=0; i < children.size(); i++) {
            setChildDetails(children, selectedChildren, i);
        }
        return selectedChildren;
    }

    public List<Element<Child>> updateChildrenExtension(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        List<Element<Child>> children = caseData.getChildren1();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();

        List<ChildExtension> allChildExtension = childExtensionEventData.getAllChildExtension();

        allChildExtension.stream()
            .filter(Objects::nonNull)
            .forEach(childExtension ->
                updateExtensionDate(childExtension, getElement(childExtension.getId(), children), defaultCompletionDate)
            );

        return children;
    }

    public List<Element<Child>> updateAllChildrenExtension(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        ChildExtension childExtensionAll = childExtensionEventData.getChildExtensionAll();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();


        List<Element<Child>> children = caseData.getChildren1();

        children.forEach(childElement -> updateExtensionDate(childExtensionAll, childElement, defaultCompletionDate));

        return children;
    }

    public List<Element<Child>> updateAllSelectedChildrenExtension(CaseData caseData) {
        List<Integer> selected = caseData.getChildExtensionEventData().getChildSelectorForExtension().getSelected();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();
        List<Element<Child>> children = caseData.getChildren1();
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        ChildExtension childExtensionAll = childExtensionEventData.getChildExtensionAll();

        selected.forEach(value -> updateExtensionDate(childExtensionAll, children.get(value), defaultCompletionDate));
        return children;
    }

    private void updateExtensionDate(ChildExtension childExtension, Element<Child> childElement, LocalDate caseCompletionDate) {
        Child child = childElement.getValue();
        ChildParty.ChildPartyBuilder childPartyBuilder = child.getParty().toBuilder();
        childPartyBuilder.extensionReason(childExtension.getCaseExtensionReasonList().name());
        LocalDate childExtensionDate = childExtension.getExtensionDateOther();

        if (EIGHT_WEEK_EXTENSION.equals(childExtension.getCaseExtensionTimeList())) {
            childExtensionDate = Optional.ofNullable(child.getParty().getCompletionDate())
                    .map(childCompletionDate -> childCompletionDate.plusWeeks(8))
                .orElseGet(() -> caseCompletionDate.plusWeeks(8));
        }

        ChildParty childParty = childPartyBuilder.completionDate(childExtensionDate).build();
        childElement.setValue(child.toBuilder().party(childParty).build());
    }

    public List<String> validateChildExtensionDate(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        int[] index = {0};

        return childExtensionEventData.getAllChildExtension().stream()
                .peek(data -> index[0]++)
                .filter(Objects::nonNull)
                .map(childExtension -> validateGroupService.validateGroup(childExtension, CaseExtensionGroup .class))
                .flatMap(List::stream)
                .map(error -> String.join(" ",  error, "for child", String.valueOf(index[0])))
                .collect(Collectors.toList());
    }
}
