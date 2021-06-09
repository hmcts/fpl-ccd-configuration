package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35bInterimSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeMessages;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C35bISODocumentParameterGenerator implements DocmosisParameterGenerator {

    private final OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;

    @Override
    public Order accept() {
        return Order.C35B_INTERIM_SUPERVISION_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        return C35bInterimSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C35B_INTERIM_SUPERVISION_ORDER.getTitle())
            .orderType(GeneratedOrderType.SUPERVISION_ORDER)
            .furtherDirections(manageOrdersEventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetailsWithEndTypeGenerator.orderDetails(
                manageOrdersEventData.getManageOrdersEndDateTypeWithEndOfProceedings(),
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
}
