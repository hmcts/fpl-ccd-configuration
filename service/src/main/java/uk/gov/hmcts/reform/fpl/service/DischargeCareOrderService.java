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
import uk.gov.hmcts.reform.fpl.utils.OrderHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DischargeCareOrderService {

    public List<GeneratedOrder> getCareOrders(CaseData caseData) {
        return caseData.getOrderCollection().stream()
            .map(Element::getValue)
            .filter(order -> OrderHelper.isOfType(order, CARE_ORDER))
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

    public List<Child> getChildrenInSelectedCareOrders(CaseData caseData) {
        return getSelectedCareOrders(caseData).stream()
            .flatMap(order -> (isEmpty(order.getChildren()) ? caseData.getAllChildren() : order.getChildren()).stream())
            .map(Element::getValue)
            .filter(distinct(child -> Arrays.asList(child.getParty().getFullName(), child.getParty().getDateOfBirth())))
            .collect(toList());
    }

    public String getOrdersLabel(List<GeneratedOrder> careOrders) {
        if (careOrders.size() == 1) {
            return format("Create discharge of care order for %s", getOrderLabel(careOrders.get(0)));
        } else {
            return range(0, careOrders.size())
                .mapToObj(index -> format("Order %d: %s, %s", index + 1, getOrderLabel(careOrders.get(index)),
                    careOrders.get(index).getDateOfIssue()))
                .collect(joining("\n"));
        }
    }

    private String getOrderLabel(GeneratedOrder order) {
        return order.getChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(Party::getFullName)
            .collect(joining(" and "));
    }

    private static <T> Predicate<T> distinct(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
