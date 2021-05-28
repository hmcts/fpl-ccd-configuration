package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.HEARING_DETAILS;

@Component
public class HearingDetailsSectionPrePopulator implements OrderSectionPrePopulator {

    @Override
    public OrderSection accept() {
        return HEARING_DETAILS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        Order type = caseData.getManageOrdersEventData().getManageOrdersType();
        return Map.of("hearingDetailsSectionSubHeader", type.getHistoryTitle());
    }
}
