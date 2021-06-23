package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35aSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeMessages;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C35aSupervisionOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private static final GeneratedOrderType TYPE = GeneratedOrderType.SUPERVISION_ORDER;

    private final OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;

    @Override
    public Order accept() {
        return Order.C35A_SUPERVISION_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C35aSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C35A_SUPERVISION_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetailsWithEndTypeGenerator.orderDetails(
                eventData.getManageOrdersEndDateTypeWithMonth(),
                OrderDetailsWithEndTypeMessages.builder()
                    .messageWithSpecifiedTime(
                        "The Court orders ${localAuthorityName} supervises the ${childOrChildren} until ${endDate}.")
                    .messageWithNumberOfMonths(
                        "The Court orders ${localAuthorityName} supervises the ${childOrChildren} for ${numOfMonths} "
                            + "months from the date of "
                            + "this order until ${endDate}.")
                    .messageWithEndOfProceedings(
                        "The Court orders ${localAuthorityName} supervises the ${childOrChildren} until "
                            + "the end of the proceedings or until a further order is made.").build(),
                caseData))
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

}
