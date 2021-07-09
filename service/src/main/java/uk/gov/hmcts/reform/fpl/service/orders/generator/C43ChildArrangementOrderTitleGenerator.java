package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.List;

@Component
public class C43ChildArrangementOrderTitleGenerator {

    public String getOrderTitle(ManageOrdersEventData eventData) {
        List<C43OrderType> orders = eventData.getManageOrdersMultiSelectListForC43();

        switch (orders.size()) {
            case 1:
                return String.format("%s order", orders.get(0).getLabel());
            case 2:
                return String.format("%s and %s order", orders.get(0).getLabel(), orders.get(1).getLabel());
            default:
                return String.format("%s, %s and %s order",
                    orders.get(0).getLabel(),
                    orders.get(1).getLabel(),
                    orders.get(2).getLabel());
        }
    }

}
