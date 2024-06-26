package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))

public class RecordFinalDecisionsService {
    public static final String CLOSE_CASE_WARNING =
        "Record which children on a case have a final order or other resolution.\n\n"
            + "If all the children have final orders or resolutions, you can close the case.\n\n"
            + "In a closed case, you can still:\n"
            + "   •  add a case note\n"
            + "   •  upload a document\n"
            + "   •  issue a C21 (blank order)\n"
            + "   •  submit a C2 application\n\n"
            + "Appeals can still be made up to 21 days after a close is marked as closed/resolved.";

    private final ChildrenService childrenService;
    private final OptionCountBuilder optionCountBuilder;

    public Map<String, Object> prePopulateFields(CaseData caseData) {
        List<Element<Child>> children = childrenService.getRemainingChildren(caseData);
        final Selector childSelector = newSelector(children.size());

        return Map.of(
            "childSelector", childSelector,
            "children_label", childrenService.getChildrenLabel(children, false),
            "close_case_label", CLOSE_CASE_WARNING
        );
    }

    public Map<String, Object> populateFields(CaseData caseData) {
        List<Element<Child>> children = childrenService.getRemainingChildren(caseData);
        List<String> childrenLabels = Arrays.asList(childrenService.getChildrenLabel(children, false).split("\n"));

        Map<String, Object> data = new HashMap<>();

        for (int i = 0; i < children.size(); i++) {
            String position = i < 10 ? "0" + i : String.valueOf(i);

            data.put("childFinalDecisionDetails" + position,
                ChildFinalDecisionDetails.builder().childNameLabel(childrenLabels.get(i)).build()
            );
        }

        if (YES.getValue().equals(caseData.getOrderAppliesToAllChildren())) {
            data.put("optionCount", optionCountBuilder.generateCode(children));
        }

        return data;
    }

    public List<Element<Child>> updateChildren(CaseData caseData) {
        List<Element<Child>> updatedChildren = childrenService.getRemainingChildren(caseData);
        RecordChildrenFinalDecisionsEventData eventData = caseData.getRecordChildrenFinalDecisionsEventData();
        List<ChildFinalDecisionDetails> childFinalDecisionDetails = eventData.getAllChildrenDecisionDetails();

        for (int i = 0; i < updatedChildren.size(); i++) {
            ChildFinalDecisionReason decisionReason = childFinalDecisionDetails.get(i) != null
                ? childFinalDecisionDetails.get(i).getFinalDecisionReason() : null;
            if (decisionReason != null) {
                updatedChildren.get(i).getValue()
                    .setFinalDecisionReason(decisionReason.getLabel());
                updatedChildren.get(i).getValue()
                    .setFinalDecisionDate(formatLocalDateToString(eventData.getFinalDecisionDate(), DATE));
            }
        }

        List<Element<Child>> allChildren = caseData.getAllChildren();
        allChildren.addAll(updatedChildren);

        return allChildren.stream().distinct().collect(Collectors.toList());
    }

    public List<String> validateChildSelector(CaseData caseData) {
        String orderAppliesToAllChildren = caseData.getOrderAppliesToAllChildren();
        Selector childSelector = caseData.getChildSelector();

        if (NO.getValue().equals(orderAppliesToAllChildren) && childSelector.getSelected().isEmpty()) {
            return List.of("Select the children with a final order or other decision");
        }

        return Collections.emptyList();
    }

    public List<String> validateFinalDecisionDate(CaseData caseData) {
        LocalDate finalDecisionDate = caseData.getRecordChildrenFinalDecisionsEventData().getFinalDecisionDate();

        if (finalDecisionDate.isAfter(LocalDate.now())) {
            return List.of("The final decision date must be in the past");
        }

        return Collections.emptyList();
    }

}
