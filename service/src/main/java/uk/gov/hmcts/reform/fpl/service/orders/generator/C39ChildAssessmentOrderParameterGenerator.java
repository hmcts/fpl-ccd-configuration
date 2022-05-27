package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C39ChildAssessmentOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C39ChildAssessmentOrderParameterGenerator implements DocmosisParameterGenerator {

    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return Order.C39_CHILD_ASSESSMENT_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C39ChildAssessmentOrderDocmosisParameters.builder()
            .orderTitle(Order.C39_CHILD_ASSESSMENT_ORDER.getTitle())
            .childrenAct(Order.C39_CHILD_ASSESSMENT_ORDER.getChildrenAct())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(buildOrderDetails(caseData))
            .build();
    }

    private String buildOrderDetails(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("The Court orders a ")
            .append(eventData.getManageOrdersChildAssessmentType().getTitle())
            .append(" of a child.\n\n")
            .append("The assessment is to begin by ")
            .append(String.format(
                formatLocalDateToString(eventData.getManageOrdersAssessmentStartDate(), DATE_WITH_ORDINAL_SUFFIX),
                getDayOfMonthSuffix(eventData.getManageOrdersAssessmentStartDate().getDayOfMonth())
                )
            )
            .append(" and last no more than ")
            .append(eventData.getManageOrdersDurationOfAssessment())
            .append(String.format(" day%s from the date it begins.\n\n",
                eventData.getManageOrdersDurationOfAssessment() > 1 ? "s" : ""))
            .append("Notice: Any person who is in a position to produce the child must do so to ")
            .append(eventData.getManageOrdersChildToBeProducedToWhom())
            .append(" and must comply with the directions in this order.");

        return orderMessageGenerator.formatOrderMessage(caseData, stringBuilder.toString());
    }
}
