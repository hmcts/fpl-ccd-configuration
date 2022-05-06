package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C36VariationOrExtensionOfSupervisionOrdersParameterGenerator implements DocmosisParameterGenerator {

    @Override
    public Order accept() {
        return Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters.builder()
            .orderTitle(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getTitle())
            .childrenAct(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getChildrenAct())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }
}
