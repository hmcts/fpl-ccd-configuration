package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;
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

import static java.util.stream.Collectors.joining;
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
    private final ValidateGroupService validateGroupService;
    private final HearingService hearingService;

    private String buildChildCaseCompletionDateLabel(CaseData caseData) {
        List<Child> children = ElementUtils.unwrapElements(caseData.getChildren1());

        int[] counter = {1};
        return children.stream()
            .map(Child::getParty)
            .map(childParty -> {
                String childCaseCompletionDate = formatLocalDateToString(
                    Optional.ofNullable(childParty.getCompletionDate())
                            .orElseGet(caseData::getDefaultCompletionDate),
                    DATE);
                return String.format("Child %d: %s: %s",
                        counter[0]++,
                        childParty.getFullName(),
                        childCaseCompletionDate);
            })
            .collect(joining(System.lineSeparator()));
    }

    public Map<String, Object> prePopulateFields(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren1();
        final Selector childSelector = newSelector(children.size());

        return Map.of(
            "childSelectorForExtension", childSelector,
            "childCaseCompletionDateLabel", buildChildCaseCompletionDateLabel(caseData),
            "shouldBeCompletedByDate", formatLocalDateToString(
                    getMaxExtendedTimeLine(caseData, caseData.getChildren1()),
                    DATE
                ),
            "extendTimelineHearingList", caseData.buildDynamicHearingList(caseData.getPastHearings(), null)
        );
    }

    public List<String> validateChildSelector(CaseData caseData) {
        String orderAppliesToAllChildren = caseData.getChildExtensionEventData().getExtensionForAllChildren();

        Selector childSelector = caseData.getChildExtensionEventData().getChildSelectorForExtension();

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
                .index(String.valueOf(++value))
                .build());
    }

    public Map<String, Object> getAllChildren(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren1();
        Map<String, Object> selectedChildren = new HashMap<>();
        for (int i = 0; i < children.size(); i++) {
            setChildDetails(children, selectedChildren, i);
        }
        return selectedChildren;
    }

    public List<Element<Child>> updateChildrenExtension(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        List<Element<Child>> children = caseData.getChildren1();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();

        List<ChildExtension> allChildExtension = childExtensionEventData.getAllChildExtension();

        LocalDate extensionDate = getExtensionDate(caseData)
            .map(d -> d.plusWeeks(8))
            .orElse(defaultCompletionDate);

        allChildExtension.stream()
            .filter(Objects::nonNull)
            .forEach(childExtension ->
                updateExtensionDate(childExtension, getElement(childExtension.getId(), children), extensionDate)
            );

        return children;
    }

    public List<Element<Child>> updateAllChildrenExtension(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        ChildExtension childExtensionAll = childExtensionEventData.getChildExtensionAll();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();

        List<Element<Child>> children = caseData.getChildren1();

        LocalDate extensionDate = getExtensionDate(caseData)
            .map(d -> d.plusWeeks(8))
            .orElse(defaultCompletionDate);

        children.forEach(childElement -> updateExtensionDate(childExtensionAll, childElement, extensionDate));

        return children;
    }

    public List<Element<Child>> updateAllSelectedChildrenExtension(CaseData caseData) {
        List<Integer> selected = caseData.getChildExtensionEventData().getChildSelectorForExtension().getSelected();
        LocalDate defaultCompletionDate = caseData.getDefaultCompletionDate();
        List<Element<Child>> children = caseData.getChildren1();
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        ChildExtension childExtensionAll = childExtensionEventData.getChildExtensionAll();

        LocalDate extensionDate = getExtensionDate(caseData)
            .map(d -> d.plusWeeks(8))
            .orElse(defaultCompletionDate);

        selected.forEach(value -> updateExtensionDate(childExtensionAll, children.get(value), extensionDate));
        return children;
    }

    private void updateExtensionDate(ChildExtension childExtension,
                                     Element<Child> childElement,
                                     LocalDate eightWeekExtensionDate) {
        Child child = childElement.getValue();
        ChildParty.ChildPartyBuilder childPartyBuilder = child.getParty().toBuilder();
        childPartyBuilder.extensionReason(childExtension.getCaseExtensionReasonList());
        LocalDate childExtensionDate = EIGHT_WEEK_EXTENSION.equals(childExtension.getCaseExtensionTimeList())
            ? eightWeekExtensionDate
            : childExtension.getExtensionDateOther();

        ChildParty childParty = childPartyBuilder.completionDate(childExtensionDate).build();
        childElement.setValue(child.toBuilder().party(childParty).build());
    }

    public List<String> validateChildExtensionDate(CaseData caseData) {
        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        String[] location = {"0"};

        return childExtensionEventData.getAllChildExtension().stream()
                .filter(Objects::nonNull)
                .map(childExtension -> {
                    location[0] = childExtension.getIndex();
                    return validateGroupService.validateGroup(childExtension, CaseExtensionGroup.class);
                })
                .flatMap(List::stream)
                .map(error -> String.join(" ",  error, "for child", location[0]))
                .collect(Collectors.toList());
    }

    public String getCaseSummaryExtensionDetails(CaseData caseData, List<Element<Child>> children1) {
        return ElementUtils.unwrapElements(children1).stream()
            .map(Child::getParty)
            .map(childParty ->
                String.join(" - ",
                    childParty.getFullName(),
                    formatLocalDateToString(Optional.ofNullable(childParty.getCompletionDate())
                            .orElse(caseData.getDefaultCompletionDate()), DATE),
                    Optional.ofNullable(childParty.getExtensionReason()).orElse(CaseExtensionReasonList.NO_EXTENSION)
                        .getLabel()
                )
            )
            .collect(joining(System.lineSeparator()));
    }

    public LocalDate getMaxExtendedTimeLine(CaseData caseData, List<Element<Child>> children1) {
        return ElementUtils.unwrapElements(children1).stream()
            .map(Child::getParty)
            .map(ChildParty::getCompletionDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElseGet(caseData::getDefaultCompletionDate);
    }

    public Optional<LocalDate> getExtensionDate(CaseData caseData) {
        ChildExtensionEventData eventData = caseData.getChildExtensionEventData();

        if (YES.equals(eventData.getExtendTimelineApprovedAtHearing())) {
            DynamicList selectedHearing = eventData.getExtendTimelineHearingList();
            Optional<Element<HearingBooking>> hearing = hearingService.findHearing(caseData, selectedHearing);
            return hearing.map(hearingBooking -> hearingBooking.getValue().getStartDate().toLocalDate());
        } else {
            return Optional.ofNullable(eventData.getExtendTimelineHearingDate());
        }
    }
}
