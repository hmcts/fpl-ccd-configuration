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
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A81PlacementBlankOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A81_PLACEMENT_BLANK_ORDER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class A81PlacementBlankOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    @Override
    public Order accept() {
        return A81_PLACEMENT_BLANK_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLaOrRelatingLa();
        String localAuthorityName = isEmpty(localAuthorityCode) ? null : laNameLookup
            .getLocalAuthorityName(localAuthorityCode);

        return A81PlacementBlankOrderDocmosisParameters.builder()
            .orderTitle(A81_PLACEMENT_BLANK_ORDER.getTitle())
            .orderType(GeneratedOrderType.BLANK_ORDER)
            .localAuthorityName(localAuthorityName)
            .recitalsOrPreamble(eventData.getManageOrdersPreamblesText())
            .orderDetails("THE COURT ORDERS THAT:\n\n" + eventData.getManageOrdersParagraphs()
                + (isEmpty(eventData.getManageOrdersCostOrders())
                ? "" : ("\n\n" + eventData.getManageOrdersCostOrders())))
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }
}
