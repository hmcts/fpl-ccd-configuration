package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.jose4j.jwk.OctJwkGenerator;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.OTHER_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

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

    public Map<String, Object> getSelectedChildren(CaseData caseData) {
        List<Integer> selected = caseData.getChildSelectorForExtension().getSelected();
        List<Element<Child>> children = caseData.getChildren1();
        Map<String, Object> selectedChildren = new HashMap<>();

        selected.forEach(value -> setChildDetails(children, selectedChildren, value));
        return selectedChildren;
    }

    private void setChildDetails(List<Element<Child>> children, Map<String, Object> selectedChildren, int value) {
        selectedChildren.put(
            String.join("", "childSelected", String.valueOf(value)),
            "Yes");

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

        int index = 0;
        for (ChildExtension childExtension: allChildExtension) {
           if (childExtension != null) {
               updateExtensionDate(childExtension, children.get(index), defaultCompletionDate);
           }
            index++;
        }
        return children;
    }

    private void updateExtensionDate(ChildExtension childExtension, Element<Child> childElement, LocalDate caseCompletionDate) {
        Child child = childElement.getValue();
        ChildParty.ChildPartyBuilder childPartyBuilder = child.getParty().toBuilder();
        LocalDate childExtensionDate = childExtension.getExtensionDateOther();

        if (EIGHT_WEEK_EXTENSION.equals(childExtension.getCaseExtensionTimeList())) {
            childExtensionDate = Optional.ofNullable(child.getParty().getCompletionDate()).map(childCompletionDate -> childCompletionDate.plusWeeks(8))
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
                .filter(childExtension -> Objects.nonNull(childExtension.getExtensionDateOther()))
                .map(childExtension -> validateGroupService.validateGroup(childExtension, CaseExtensionGroup .class))
                .flatMap(List::stream)
                .map(error -> String.join(" ",  error, "for child", String.valueOf(index[0])))
                .collect(Collectors.toList());
    }
}
