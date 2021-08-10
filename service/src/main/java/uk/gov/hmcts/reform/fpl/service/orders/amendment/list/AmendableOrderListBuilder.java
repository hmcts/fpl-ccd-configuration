package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendableOrderListBuilder {
    private final DynamicListService listService;
    private final List<AmendableListItemProvider> providers;

    public DynamicList buildList(CaseData caseData) {

        Comparator<Element<? extends AmendableOrder>> comparator = comparing(
            order -> order.getValue().amendableSortDate(), nullsLast(reverseOrder())
        );

        comparator = comparator.thenComparing(order -> order.getValue().asLabel(), nullsLast(naturalOrder()));

        List<Element<? extends AmendableOrder>> amendableOrders = providers.stream()
            .map(provider -> provider.provideListItems(caseData))
            .flatMap(Collection::stream)
            .sorted(comparator)
            .collect(Collectors.toList());

        return listService.asDynamicList(
            amendableOrders,
            order -> order.getId().toString(),
            order -> order.getValue().asLabel()
        );
    }
}
