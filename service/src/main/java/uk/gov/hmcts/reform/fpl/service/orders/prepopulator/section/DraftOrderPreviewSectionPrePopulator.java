package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrderPreviewSectionPrePopulator implements OrderSectionPrePopulator {

    private final OrderDocumentGenerator orderDocumentGenerator;

    @Override
    public OrderSection accept() {
        return OrderSection.REVIEW;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData,
                                           CaseDetails caseDetails) {
        DocmosisDocument draftOrder = orderDocumentGenerator.generate(caseData.getManageOrdersType(), caseDetails);

        return Map.of();
    }
}
