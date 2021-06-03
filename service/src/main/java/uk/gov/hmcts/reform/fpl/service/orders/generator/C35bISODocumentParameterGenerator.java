package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35bInterimSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.generics.ManageOrderWithEndOfProceedingsDocumentParameterGenerator;

import java.util.List;

@Component
public class C35bISODocumentParameterGenerator extends ManageOrderWithEndOfProceedingsDocumentParameterGenerator {

    public C35bISODocumentParameterGenerator(
        ChildrenService childrenService, LocalAuthorityNameLookupConfiguration laNameLookup) {
        super(childrenService, laNameLookup);
    }

    @Override
    public Order accept() {
        return Order.C35B_INTERIM_SUPERVISION_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    @Override
    protected DocmosisParameters docmosisParameters(ManageOrdersEventData manageOrdersEventData,
                                                    String localAuthorityCode,
                                                    String localAuthorityName,
                                                    List<Element<Child>> selectedChildren) {
        return C35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C35B_INTERIM_SUPERVISION_ORDER.getTitle())
            .orderType(GeneratedOrderType.SUPERVISION_ORDER)
            .furtherDirections(manageOrdersEventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName, manageOrdersEventData))
            .build();
    }
}
