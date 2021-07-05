package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendableOrderListBuilder {
    private final DynamicListService listService;

    public Optional<DynamicList> buildList(CaseData caseData) {
        if (CLOSED == caseData.getState()) {
            return Optional.empty();
        }

        List<Element<GeneratedOrder>> generatedOrders = caseData.getOrderCollection();
        List<Element<HearingOrder>> cmos = caseData.getSealedCMOs();
        UrgentHearingOrder uho = caseData.getUrgentHearingOrder();
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder();

        List<Element<? extends AmendableOrder>> amendableOrders = new ArrayList<>(generatedOrders);
        amendableOrders.addAll(cmos);

        if (null != uho) {
            amendableOrders.add(element(UrgentHearingOrder.COLLECTION_ID, uho));
        }

        if (null != sdo && sdo.isSealed()) {
            amendableOrders.add(element(StandardDirectionOrder.COLLECTION_ID, sdo));
        }

        Comparator<Element<? extends AmendableOrder>> comparator = comparing(
            order -> order.getValue().amendableSortDate(), reverseOrder()
        );

        comparator = comparator.thenComparing(order -> order.getValue().asLabel());

        amendableOrders.sort(comparator);

        return Optional.of(listService.asDynamicList(
            amendableOrders,
            order -> order.getId().toString(),
            order -> order.getValue().asLabel()
        ));
    }
}
