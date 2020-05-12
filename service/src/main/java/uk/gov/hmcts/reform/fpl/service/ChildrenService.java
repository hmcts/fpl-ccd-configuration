package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
public class ChildrenService {

    public String getChildrenLabel(List<Element<Child>> children) {
        if (isEmpty(children)) {
            return "No children in the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            final String name = children.get(i).getValue().getParty().getFullName();
            builder.append(String.format("Child %d: %s%n", i + 1, name));
        }

        return builder.toString();
    }

    public void addPageShowToCaseDetails(CaseDetails caseDetails, List<Element<Child>> children) {
        caseDetails.getData().put("pageShow", children.size() <= 1 ? "No" : "Yes");
    }

    public List<Element<Child>> updateFinalOrderIssued(List<Element<Child>> children,
        String orderAppliesToAllChildren, ChildSelector childSelector) {

        if (YES.getValue().equals(orderAppliesToAllChildren)) {
            return children.stream()
                .map(child -> updateFinalOrderIssuedOnChild(child, YES))
                .collect(Collectors.toList());
        } else {
            List<Integer> selectedChildren = childSelector != null ? childSelector.getSelected() : new ArrayList<>();
            return IntStream.range(0, children.size())
                .mapToObj(index -> {
                    Element<Child> child = children.get(index);
                    if (!selectedChildren.isEmpty() && selectedChildren.contains(index)) {
                        return updateFinalOrderIssuedOnChild(child, YES);
                    } else if (StringUtils.isEmpty(child.getValue().getParty().getFinalOrderIssued())) {
                        return updateFinalOrderIssuedOnChild(child, NO);
                    }
                    return child;
                })
                .collect(Collectors.toList());
        }
    }

    private Element<Child> updateFinalOrderIssuedOnChild(Element<Child> child, YesNo yesNo) {
        ChildParty updatedChildParty = child.getValue().getParty().toBuilder()
            .finalOrderIssued(yesNo.getValue()).build();
        Child updatedChild = child.getValue().toBuilder()
            .party(updatedChildParty).build();
        return Element.<Child>builder()
            .id(child.getId())
            .value(updatedChild)
            .build();
    }
}
