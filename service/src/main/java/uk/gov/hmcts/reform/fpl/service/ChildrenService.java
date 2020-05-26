package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            Child child = children.get(i).getValue();
            final String name = child.getParty().getFullName();
            builder.append(String.format("Child %d: %s", i + 1, name));
            if (YES.getValue().equals(child.getFinalOrderIssued()) && child.getFinalOrderIssuedType() != null) {
                builder.append(String.format(" - %s issued", child.getFinalOrderIssuedType()));
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public void addPageShowToCaseDetails(CaseDetails caseDetails, List<Element<Child>> children) {
        caseDetails.getData().put("pageShow", children.size() <= 1 ? "No" : "Yes");
    }

    public boolean allChildrenHaveFinalOrder(List<Element<Child>> children) {
        if (children == null || children.isEmpty()) {
            return false;
        }
        return children.stream().allMatch(child -> YES.getValue().equals(child.getValue().getFinalOrderIssued()));
    }

    public List<Element<Child>> updateFinalOrderIssued(GeneratedOrderType orderType, List<Element<Child>> children,
        String orderAppliesToAllChildren, ChildSelector childSelector) {
        if (YES.getValue().equals(orderAppliesToAllChildren)) {
            children.forEach(child -> {
                child.getValue().setFinalOrderIssued(YES.getValue());
                child.getValue().setFinalOrderIssuedType(orderType.getLabel());
            });
        } else {
            List<Integer> selectedChildren = childSelector != null ? childSelector.getSelected() : new ArrayList<>();
            for (int i = 0; i < children.size(); i++) {
                Child child = children.get(i).getValue();
                if (!selectedChildren.isEmpty() && selectedChildren.contains(i)) {
                    child.setFinalOrderIssued(YES.getValue());
                    child.setFinalOrderIssuedType(orderType.getLabel());
                } else if (StringUtils.isEmpty(child.getFinalOrderIssued())) {
                    child.setFinalOrderIssued(NO.getValue());
                }
            }
        }
        return children;
    }

    public String getRemainingChildCount(List<Element<Child>> allChildren) {
        List<String> remainingChildIndex = new ArrayList<>();
        for (int i = 0; i < allChildren.size(); i++) {
            if (!YES.getValue().equals(allChildren.get(i).getValue().getFinalOrderIssued())) {
                remainingChildIndex.add(String.valueOf(i));
            }
            if (remainingChildIndex.size() > 2) {
                return "";
            }
        }

        return (remainingChildIndex.size() == 1) ? remainingChildIndex.get(0) : "";
    }

    public String getRemainingChildren(List<Element<Child>> allChildren) {
        return allChildren.stream()
            .filter(child -> !YES.getValue().equals(child.getValue().getFinalOrderIssued()))
            .map(child -> child.getValue().getParty().getFullName())
            .collect(Collectors.joining("\n"));
    }

    public String getFinalOrderIssuedChildren(List<Element<Child>> allChildren) {
        return allChildren.stream()
            .filter(child -> YES.getValue().equals(child.getValue().getFinalOrderIssued()))
            .map(child -> {
                StringBuilder builder = new StringBuilder();
                builder.append(child.getValue().getParty().getFullName());
                if (child.getValue().getFinalOrderIssuedType() != null) {
                    builder.append(String.format(" - %s issued", child.getValue().getFinalOrderIssuedType()));

                }
                return builder.toString();
            })
            .collect(Collectors.joining("\n"));
    }
}
