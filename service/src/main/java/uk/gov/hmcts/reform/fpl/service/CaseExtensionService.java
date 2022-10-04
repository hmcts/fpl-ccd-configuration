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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionService {

    private final ChildrenService childrenService;
    private final OptionCountBuilder optionCountBuilder;

    public LocalDate getCaseCompletionDate(CaseData caseData) {
        if (caseData.getCaseExtensionTimeList().equals(EIGHT_WEEK_EXTENSION)) {
            if (caseData.getCaseExtensionTimeConfirmationList().equals(EIGHT_WEEK_EXTENSION)) {
                return getCaseCompletionDateFor8WeekExtension(caseData);
            }
            return caseData.getEightWeeksExtensionDateOther();
        }
        return caseData.getExtensionDateOther();
    }

    public LocalDate getCaseCompletionDateFor8WeekExtension(CaseData caseData) {
        return getCaseShouldBeCompletedByDate(caseData).plusWeeks(8);
    }

    public LocalDate getCaseShouldBeCompletedByDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseCompletionDate()).orElse(caseData.getDateSubmitted().plusWeeks(26));
    }


    public String buildChildCaseCompletionDateLabel(CaseData caseData) {
        List<Child> children = caseData.getChildren1().stream().map(Element::getValue).collect(Collectors.toList());

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
}
