package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DischargeCareOrderService {

    public List<GeneratedOrder> getCareOrders(CaseData caseData) {
        return caseData.getOrderCollection().stream()
            .map(Element::getValue)
            .filter(go -> ("Final care order".equals(go.getType()) || "Interim care order".equals(go.getType())))
            .collect(toList());
    }

    public List<GeneratedOrder> getSelectedCareOrders(CaseData caseData) {
        List<GeneratedOrder> careOrders = getCareOrders(caseData);
        Selector careOrderSelector = caseData.getCareOrderSelector();

        if (careOrderSelector == null) {
            return careOrders;
        } else {
            return caseData.getCareOrderSelector().getSelected().stream()
                .map(careOrders::get)
                .collect(toList());
        }
    }

    public List<Element<Child>> getSelectedChildren(CaseData caseData) {
        return getSelectedCareOrders(caseData).stream()
            .flatMap(order ->
                order.getChildren().isEmpty() ? caseData.getAllChildren().stream() : order.getChildren().stream())
            .distinct()
            .collect(toList());
    }

    public String getOrdersLabel(List<GeneratedOrder> careOrders) {
        if (careOrders.size() == 1) {
            return format("Create discharge of care order for %s", getOrderLabel(careOrders.get(0)));
        } else {
            return range(0, careOrders.size())
                .mapToObj(index -> format("Order %d: %s, %s", index + 1, getOrderLabel(careOrders.get(index)),
                    careOrders.get(index).getDateOfIssue()))
                .collect(joining(lineSeparator()));
        }
    }

    private String getOrderLabel(GeneratedOrder order) {
        return order.getChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(Party::getFullName)
            .collect(joining(" and "));
    }
}
