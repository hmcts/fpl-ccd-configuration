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
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendableOrderListBuilder {
    private final DynamicListService listService;
    private final List<AmendableListItemProvider> providers;

    public Optional<DynamicList> buildList(CaseData caseData) {
        if (CLOSED == caseData.getState()) {
            return Optional.empty();
        }

        Comparator<Element<? extends AmendableOrder>> comparator = comparing(
            order -> order.getValue().amendableSortDate(), reverseOrder()
        );

        comparator = comparator.thenComparing(order -> order.getValue().asLabel());

        List<Element<? extends AmendableOrder>> amendableOrders = providers.stream()
            .map(provider -> provider.provideListItems(caseData))
            .flatMap(Collection::stream)
            .sorted(comparator)
            .collect(Collectors.toList());

        return Optional.of(listService.asDynamicList(
            amendableOrders,
            order -> order.getId().toString(),
            order -> order.getValue().asLabel()
        ));
    }
}
