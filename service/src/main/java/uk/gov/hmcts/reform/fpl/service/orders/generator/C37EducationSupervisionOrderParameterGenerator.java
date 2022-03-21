package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C37EducationSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C37EducationSupervisionOrderParameterGenerator implements DocmosisParameterGenerator {

    private static final String ORDER_HEADER = "Warning\n";
    private static final String ORDER_MESSAGE = "A parent of the ${childOrChildren} may be guilty of an offence "
                                                + "if he or she persistently fails to comply with a direction given "
                                                + "by the supervisor under this order while it is in force "
                                                + "(Paragraph 18 Schedule 3 Children Act 1989). ";

    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C37EducationSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL.getTitle())
            .childrenAct(Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL.getChildrenAct())
            .orderHeader(ORDER_HEADER)
            .orderMessage(orderMessageGenerator.formatOrderMessage(caseData, ORDER_MESSAGE))
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(buildOrderDetails(caseData))
            .build();
    }

    private String buildOrderDetails(CaseData caseData){
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
            .append("The Court was satisfied that the ${childOrChildren} ${childWasOrWere} of compulsory school age ")
            .append("and ${childWasOrWere} not being properly educated.\n\n");

        stringBuilder.append("The Court orders ")
            .append(eventData.getManageOrdersLedName())
            .append(" local education authority to supervise the ${childOrChildren} ");


        switch(eventData.getManageOrdersEndDateWithEducationAge()){
            case TWELVE_MONTHS_FROM_DATE_OF_ORDER:
                stringBuilder.append("for a period of 12 months beginning on the date of this order.");
                break;
            case UNTIL_END_OF_COMPULSORY_EDUCATION_AGE:
                stringBuilder.append(orderMessageGenerator.formatOrderMessage(caseData,
                    "until the ${childOrChildren} ${childWasOrWere} no longer of compulsory school age."));
                break;
        }

        return orderMessageGenerator.formatOrderMessage(caseData, stringBuilder.toString());
    }
}
