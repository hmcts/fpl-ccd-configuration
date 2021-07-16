package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AvailableTranslationListBuilder {

    private final DynamicListService listService;
    private final TranslatableListItemProviders providers;

    public DynamicList build(CaseData caseData) {
//        Comparator<Element<? extends AmendableOrder>> comparator = comparing(
//            order -> order.getValue().amendableSortDate(), nullsLast(reverseOrder())
//        );
//
//        comparator = comparator.thenComparing(order -> order.getValue().asLabel(), nullsLast(naturalOrder()));

        List<Element<? extends TranslatableItem>> translatableOrders = providers.getAll().stream()
            .map(provider -> provider.provideListItems(caseData))
            .flatMap(Collection::stream)
//            .sorted(comparator)
            .collect(Collectors.toList());

        return listService.asDynamicList(
            translatableOrders,
            order -> order.getId().toString(),
            order -> order.getValue().asLabel()
        );
    }

}
