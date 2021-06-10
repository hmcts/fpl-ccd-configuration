package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C33InterimCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeMessages;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C33InterimCareOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private static final GeneratedOrderType TYPE = GeneratedOrderType.CARE_ORDER;

    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;

    @Override
    public Order accept() {
        return Order.C33_INTERIM_CARE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C33InterimCareOrderDocmosisParameters.builder()
            .orderTitle(Order.C33_INTERIM_CARE_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .exclusionClause(eventData.getManageOrdersExclusionDetails())
            .localAuthorityName(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .orderDetails(orderDetailsWithEndTypeGenerator.orderDetails(
                eventData.getManageOrdersEndDateTypeWithEndOfProceedings(),
                OrderDetailsWithEndTypeMessages.builder()
                    .messageWithSpecifiedTime(
                        "The Court orders that the ${childOrChildren} ${childIsOrAre} placed in the care of "
                            + "${localAuthorityName} until ${endDate}.")
                    .messageWithEndOfProceedings(
                        "The Court orders that the ${childOrChildren} ${childIsOrAre} placed in the care of "
                            + "${localAuthorityName} until "
                            + "the end of the proceedings or until a further order is made.").build(),
                caseData))
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }
}
