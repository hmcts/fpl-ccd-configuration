package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C36VariationOrExtensionOfSupervisionOrdersParameterGenerator implements DocmosisParameterGenerator {

    private static final String FULLSTOP_NEW_LINE = ".\n\n";
    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters.builder()
            .orderTitle(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getTitle())
            .childrenAct(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getChildrenAct())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(buildOrderDetails(caseData))
            .build();
    }

    private String buildOrderDetails(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        StringBuilder stringBuilder = new StringBuilder();

        switch (eventData.getManageOrdersSupervisionOrderType()) {
            case VARIATION_OF_SUPERVISION_ORDER:
                stringBuilder.append("The Court varies the Supervision Order ");
                break;
            case EXTENSION_OF_SUPERVISION_ORDER:
                stringBuilder.append("The Court extends the Supervision Order ");
                break;
        }

        stringBuilder
            .append("made by this Court, ${courtName} on ")
            .append(dateBuilder(eventData.getManageOrdersSupervisionOrderApprovalDate()))
            .append(FULLSTOP_NEW_LINE);

        stringBuilder
            .append("The Court orders ${localAuthorityName} to supervise the ${childOrChildren}")
            .append(FULLSTOP_NEW_LINE);

        stringBuilder
            .append("The Court directs ")
            .append(eventData.getManageOrdersSupervisionOrderCourtDirection())
            .append(FULLSTOP_NEW_LINE);

        stringBuilder
            .append("This order ends on ")
            .append(dateBuilder(eventData.getManageOrdersSupervisionOrderEndDate()))
            .append(FULLSTOP_NEW_LINE);

        return orderMessageGenerator.formatOrderMessage(caseData, stringBuilder.toString());
    }

    private String dateBuilder(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
