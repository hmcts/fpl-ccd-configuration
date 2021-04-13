package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ORDER_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
public class OrderDetailsSectionPrePopulator implements OrderSectionPrePopulator {
    @Override
    public OrderSection accept() {
        return ORDER_DETAILS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Selector oldSelector = defaultIfNull(caseData.getChildSelector(), Selector.builder().build());
        Selector regeneratedSelector = newSelector(caseData.getAllChildren().size());
        regeneratedSelector.setSelected(oldSelector.getSelected());
        // REFACTOR: 12/04/2021 remove and replace with field interpolation once EUI-3581 is complete
        Order type = caseData.getManageOrdersEventData().getManageOrdersType();
        return Map.of("orderDetailsSectionSubHeader", type.getHistoryTitle(), "childSelector", regeneratedSelector);
    }
}
