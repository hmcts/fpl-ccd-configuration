package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C42FamilyAssistanceOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C42FamilyAssistanceOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private final OrderMessageGenerator orderMessageGenerator;

    public static final String RECITALS_MESSAGE = "The Court orders an officer of the service to be made available to "
        + "advise, assist and, where appropriate, befriend";

    public static final String CONDITIONS_MESSAGE = "Where - \n"
        + "(a) there are no proceedings pending under Part 2 Children Act 1989; \n"
        + "(b) an officer of the service / Welsh family proceedings officer made available under this order is given "
        + "cause to suspect, whilst this order is in force, that the child concerned is at risk of harm; and \n"
        + "(c) as a result, the officer makes a risk assessment under section 16A of that Act,";

    public static final String DIRECTIONS_MESSAGE = "the officer may apply to the court for it to revive the previous "
        + "proceedings and to consider that risk assessment and give such directions as the court thinks necessary.";

    public static final String NOTICE_MESSAGE = "This order will have effect for 12 months from the date "
        + "ordered on, or such lesser period as specified.";

    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    @Override
    public Order accept() {
        return Order.C42_FAMILY_ASSISTANCE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        return C42FamilyAssistanceOrderDocmosisParameters.builder()
            .orderTitle("Family Assistance Order")
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(buildOrderDetails(eventData))
            .noticeHeader("Notice \n")
            .noticeMessage(NOTICE_MESSAGE)
            .localAuthorityName(localAuthorityName)
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    private String buildOrderDetails(ManageOrdersEventData eventData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getOrderRecitalsAndPreambles(eventData));
        stringBuilder.append(getOrderDirections(eventData));
        stringBuilder.append(getOrderEnd(eventData));

        return stringBuilder.toString();
    }

    private String getOrderRecitalsAndPreambles(ManageOrdersEventData eventData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(RECITALS_MESSAGE + "\n");

        if (isNotEmpty(eventData.getManageOrdersPartyToBeBefriended1().getValueLabel())) {
            stringBuilder.append(eventData.getManageOrdersPartyToBeBefriended1().getValueLabel() + "\n");
        }
        if (isNotEmpty(eventData.getManageOrdersPartyToBeBefriended2())) {
            stringBuilder.append(eventData.getManageOrdersPartyToBeBefriended2().getValueLabel() + "\n");
        }
        if (isNotEmpty(eventData.getManageOrdersPartyToBeBefriended3())) {
            stringBuilder.append(eventData.getManageOrdersPartyToBeBefriended3().getValueLabel() + "\n");
        }
        return stringBuilder.toString();
    }

    private String getOrderDirections(ManageOrdersEventData eventData) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nThe Court directs\n");
        stringBuilder.append(CONDITIONS_MESSAGE + "\n\n");
        stringBuilder.append(DIRECTIONS_MESSAGE + "\n\n");
        stringBuilder.append(eventData.getManageOrdersFurtherDirections());

        return stringBuilder.toString();
    }

    private String getOrderEnd(ManageOrdersEventData eventData) {
        return "\n\nThis order ends on " + String.format(
            formatLocalDateToString(eventData.getManageOrdersFamilyAssistanceEndDate(), DATE_WITH_ORDINAL_SUFFIX),
            getDayOfMonthSuffix(eventData.getManageOrdersFamilyAssistanceEndDate().getDayOfMonth())) + ".";
    }
}
