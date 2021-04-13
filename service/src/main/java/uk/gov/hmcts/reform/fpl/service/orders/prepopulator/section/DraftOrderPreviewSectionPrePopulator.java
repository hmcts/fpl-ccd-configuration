package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrderPreviewSectionPrePopulator implements OrderSectionPrePopulator {

    private static final String ORDER_PREVIEW_FIELD = "orderPreview";

    private final OrderCreationService orderCreationService;

    @Override
    public OrderSection accept() {
        return OrderSection.REVIEW;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        DocumentReference orderPreview = orderCreationService.createOrderDocument(caseData, DRAFT, PDF);
        return Map.of(ORDER_PREVIEW_FIELD, orderPreview);
    }
}
