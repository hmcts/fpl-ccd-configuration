package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.OTHER_DETAILS;

@Component
public class OtherDetailsSectionPrePopulator implements OrderSectionPrePopulator {
    @Override
    public OrderSection accept() {
        return OTHER_DETAILS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        return Map.of("pageShow", caseData.getAllOthers().size() == 0 ? "NO" : "YES");
    }
}
