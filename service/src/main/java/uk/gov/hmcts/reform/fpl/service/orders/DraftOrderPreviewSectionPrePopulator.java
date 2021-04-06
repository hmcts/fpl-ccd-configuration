package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrderPreviewSectionPrePopulator implements OrderSectionPrePopulator {

    private final OrderDocumentGenerator orderDocumentGenerator;

    @Override
    public OrderSection accept() {
        return OrderSection.SECTION_4;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData,
                                           CaseDetails caseDetails) {
        Order order = Order.valueOf((String) caseDetails.getData().get("manageOrdersType"));
        DocmosisDocument draftOrder = orderDocumentGenerator.generate(order, caseDetails);

        return Map.of();
    }
}
