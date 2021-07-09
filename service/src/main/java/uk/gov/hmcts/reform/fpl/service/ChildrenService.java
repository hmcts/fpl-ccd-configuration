package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
public class ChildrenService {

    public String getChildrenLabel(List<Element<Child>> children, boolean closable) {
        if (isEmpty(children)) {
            return "No children in the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i).getValue();

            builder.append(String.format("Child %d: %s", i + 1, child.asLabel()));
            if (closable && child.getFinalOrderIssuedType() != null) {
                builder.append(String.format(" - %s issued", child.getFinalOrderIssuedType()));
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public boolean allChildrenHaveFinalOrder(List<Element<Child>> children) {
        if (children == null || children.isEmpty()) {
            return false;
        }
        return children.stream().allMatch(child -> YES.getValue().equals(child.getValue().getFinalOrderIssued()));
    }

    public List<Element<Child>> updateFinalOrderIssued(CaseData caseData) {
        List<Element<Child>> childrenToIssueFinalOrder = getSelectedChildrenForIssuingFinalOrder(caseData);
        return updateFinalOrderIssued(
            caseData.getManageOrdersEventData().getManageOrdersType().getTitle(),
            caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(),
            childrenToIssueFinalOrder
        );
    }

    public List<Element<Child>> updateFinalOrderIssued(String orderLabel,
                                                       List<Element<Child>> children,
                                                       String orderAppliesToAllChildren,
                                                       List<Element<Child>> selectedChildren) {

        if (YES.getValue().equals(orderAppliesToAllChildren)) {
            children.forEach(child -> {
                child.getValue().setFinalOrderIssued(YES.getValue());
                child.getValue().setFinalOrderIssuedType(orderLabel);
            });
        } else {
            children.forEach(child -> {
                boolean childWasSelected = selectedChildren.contains(child);
                if (childWasSelected) {
                    child.getValue().setFinalOrderIssued(YES.getValue());
                    child.getValue().setFinalOrderIssuedType(orderLabel);
                }
            });
        }

        setFinalOrderNotIssuedForChildrenWithNoFinalOrderInformation(children);

        return children;
    }

    private void setFinalOrderNotIssuedForChildrenWithNoFinalOrderInformation(List<Element<Child>> children) {
        children.forEach(child -> {
            boolean finalOrderForChildWasNotSet = StringUtils.isEmpty(child.getValue().getFinalOrderIssued());
            if (finalOrderForChildWasNotSet) {
                child.getValue().setFinalOrderIssued(NO.getValue());
            }
        });
    }

    /**
     * Returns the index of the only child without a final order issued against them.
     * If there are multiple children then an empty optional is returned instead.
     * If there are no children then an empty optional is returned.
     *
     * @param children List of {@link Child} to search
     * @return index of remaining child wrapped in an optional
     */
    public Optional<Integer> getRemainingChildIndex(List<Element<Child>> children) {
        Optional<Integer> remainingChildIndex = Optional.empty();
        for (int i = 0; i < children.size(); i++) {
            if (!YES.getValue().equals(children.get(i).getValue().getFinalOrderIssued())) {
                if (remainingChildIndex.isEmpty()) {
                    remainingChildIndex = Optional.of(i);
                } else {
                    return Optional.empty();
                }
            }
        }

        return remainingChildIndex;
    }

    public String getRemainingChildrenNames(List<Element<Child>> children) {
        return children.stream()
            .filter(child -> !YES.getValue().equals(child.getValue().getFinalOrderIssued()))
            .map(child -> child.getValue().getParty().getFullName())
            .collect(Collectors.joining("\n"));
    }

    public String getFinalOrderIssuedChildrenNames(List<Element<Child>> children) {
        return children.stream()
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

    public List<Integer> getIndexesOfChildrenWithFinalOrderIssued(CaseData caseData) {
        return range(0, caseData.getAllChildren().size())
            .filter(idx -> YES.getValue().equals(caseData.getAllChildren().get(idx).getValue().getFinalOrderIssued()))
            .boxed()
            .collect(toList());
    }

    public List<Element<Child>> getSelectedChildren(CaseData caseData) {
        return getSelectedChildren(caseData.getAllChildren(), caseData.getChildSelector(),
            caseData.getOrderAppliesToAllChildren(), caseData.getRemainingChildIndex());
    }

    private List<Element<Child>> getSelectedChildren(List<Element<Child>> children, Selector selector,
                                                     String appliesToAllChildren, String remainingChildIndex) {

        if (isNotBlank(remainingChildIndex)) {
            return List.of(children.get(Integer.parseInt(remainingChildIndex)));
        }

        if (useAllChildren(appliesToAllChildren)) {
            return children;
        }

        return selector.getSelected().stream()
            .map(children::get)
            .collect(toList());
    }

    private boolean useAllChildren(String appliesToAllChildren) {
        // If there is only one child in the case then the choice will be null
        return appliesToAllChildren == null || "Yes".equals(appliesToAllChildren);
    }

    public List<Element<Child>> getSelectedChildrenForIssuingFinalOrder(CaseData caseData) {
        String remainingChildIndex = caseData.getRemainingChildIndex();
        Selector childSelector = caseData.getChildSelector();
        List<Element<Child>> children = caseData.getAllChildren();

        List<Integer> selectedChildren;
        if (StringUtils.isNotBlank(remainingChildIndex)) {
            selectedChildren = List.of(Integer.parseInt(remainingChildIndex));
        } else if (childSelector != null) {
            selectedChildren = childSelector.getSelected();
        } else {
            selectedChildren = new ArrayList<>();
        }

        return selectedChildren.stream()
            .map(children::get)
            .collect(Collectors.toList());
    }

}
