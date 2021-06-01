package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
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

    private C35bInterimSupervisionOrderDocmosisParameters c35bInterimSupervisionOrderDocmosisParameters;
    private static Order ORDER = Order.C35B_INTERIM_SUPERVISION_ORDER;

    public C35bISODocumentParameterGenerator(
        ChildrenService childrenService, LocalAuthorityNameLookupConfiguration laNameLookup) {
        super(childrenService, laNameLookup);
    }

    @Override
    public Order accept() {
        return ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    @Override
    protected DocmosisParameters docmosisParameters(ManageOrdersEventData eventData,
                                                    String localAuthorityCode,
                                                    String localAuthorityName,
                                                    List<Element<Child>> selectedChildren) {
        return c35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle(ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName, eventData))
            .build();
    }

    @Override
    protected String orderDetails(int numOfChildren, String localAuthorityName, ManageOrdersEventData eventData) {
        return super.orderDetails(numOfChildren, localAuthorityName, eventData);
    }
}
