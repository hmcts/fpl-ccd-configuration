package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.ArrayList;
import java.util.List;

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
            children.forEach(child -> child.getValue().setFinalOrderIssued(YES.getValue()));
        } else {
            List<Integer> selectedChildren = childSelector != null ? childSelector.getSelected() : new ArrayList<>();
            for (int i = 0; i < children.size(); i++) {
                Child child = children.get(i).getValue();
                if (!selectedChildren.isEmpty() && selectedChildren.contains(i)) {
                    child.setFinalOrderIssued(YES.getValue());
                } else if (StringUtils.isEmpty(child.getFinalOrderIssued())) {
                    child.setFinalOrderIssued(NO.getValue());
                }
            }
        }
        return children;
    }
}
