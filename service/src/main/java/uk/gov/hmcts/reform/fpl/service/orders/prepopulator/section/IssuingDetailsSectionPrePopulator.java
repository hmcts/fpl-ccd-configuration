package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ISSUING_DETAILS;

@Component
public class IssuingDetailsSectionPrePopulator implements OrderSectionPrePopulator {
    @Override
    public OrderSection accept() {
        return ISSUING_DETAILS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        if (State.CLOSED == caseData.getState()) {
            return Map.of(
                "manageOrdersType", C21_BLANK_ORDER,
                "issuingDetailsSectionSubHeader", C21_BLANK_ORDER.getHistoryTitle());
        } else {
            Order type = caseData.getManageOrdersEventData().getManageOrdersType();
            // REFACTOR: 12/04/2021 remove and replace with field interpolation once EUI-3581 is complete
            return Map.of("issuingDetailsSectionSubHeader", type.getHistoryTitle());
        }
    }
}
