package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.Jurisdiction;
import uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C26SecureAccommodationOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeMessages;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.Jurisdiction.ENGLAND;
import static uk.gov.hmcts.reform.fpl.enums.Jurisdiction.WALES;
import static uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation.ABSCOND;
import static uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation.INJURY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Component
@RequiredArgsConstructor
public class C26SecureAccommodationOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private static final Map<Jurisdiction, String> ORDER_ACT_PER_JURISDICTION = Map.of(
        ENGLAND, "Section 25 Children Act 1989",
        WALES, "Section 119 of the Social Services and Wellbeing (Wales) Act 2014"
    );

    private static final String NEW_PARAGRAPH = "\n\n";

    private final OrderDetailsWithEndTypeGenerator orderDetailsWithEndTypeGenerator;
    private final ChildrenSmartSelector childrenSmartSelector;

    @Override
    public Order accept() {
        return Order.C26_SECURE_ACCOMMODATION_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String orderDetails = buildOrderDetails(caseData, eventData);

        Jurisdiction orderJurisdiction = eventData.getManageOrdersOrderJurisdiction();
        String orderAct = ORDER_ACT_PER_JURISDICTION.get(orderJurisdiction);

        return C26SecureAccommodationOrderDocmosisParameters.builder()
            .orderTitle(Order.C26_SECURE_ACCOMMODATION_ORDER.getTitle())
            .childrenAct(orderAct)
            .dateOfIssue(formatLocalDateTimeBaseUsingFormat(eventData.getManageOrdersApprovalDateTime(), DATE_TIME))
            .orderDetails(orderDetails)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .build();
    }

    private String buildOrderDetails(CaseData caseData, ManageOrdersEventData eventData) {
        StringBuilder orderDetails = new StringBuilder();
        String courtAuthorisationMessage = buildCourtAuthorisationMessage(caseData, eventData);
        orderDetails.append(courtAuthorisationMessage);

        ChildGender selectedChildGender = childrenSmartSelector.getSelectedChildren(caseData).stream().findFirst()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(ChildParty::getGender)
            .map(ChildGender::fromLabel)
            .orElseThrow();

        StringBuffer orderGrounds = buildOrderGrounds(eventData, selectedChildGender);
        orderDetails.append(orderGrounds);

        if ("No".equalsIgnoreCase(eventData.getManageOrdersIsChildRepresented())) {
            orderDetails.append("The Court was satisfied that the child, not being legally represented, has been "
                + "informed of their right to apply for legal aid and having had the opportunity to apply, "
                + "had refused or failed to apply.");
        }

        return orderDetails.toString();
    }

    private String buildCourtAuthorisationMessage(CaseData caseData, ManageOrdersEventData eventData) {
        StringBuilder courtAuthorisationMessageBuilder = new StringBuilder();
        if (eventData.getManageOrdersIsByConsent().equalsIgnoreCase("Yes")) {
            courtAuthorisationMessageBuilder.append("By consent, ");
        }
        courtAuthorisationMessageBuilder.append(
            "The Court authorises ${localAuthorityName} to keep the child in secure accommodation ");
        courtAuthorisationMessageBuilder.append("%s");
        courtAuthorisationMessageBuilder.append(NEW_PARAGRAPH);
        String courtAuthorisationMessage = courtAuthorisationMessageBuilder.toString();

        return orderDetailsWithEndTypeGenerator.orderDetails(eventData.getManageOrdersEndDateTypeWithMonth(),
            OrderDetailsWithEndTypeMessages.builder()
                .messageWithSpecifiedTime(String.format(courtAuthorisationMessage, "until ${endDate}."))
                .messageWithNumberOfMonths(String.format(courtAuthorisationMessage,
                    "for ${decoratedNumberOfMonths} from the date of this order."))
                .build(),
            caseData);
    }

    private StringBuffer buildOrderGrounds(ManageOrdersEventData eventData, ChildGender selectedChildGender) {
        StringBuffer orderGrounds = new StringBuffer();

        orderGrounds.append("This order has been made on the ground that ");

        ReasonForSecureAccommodation reasonForOrder = eventData.getManageOrdersReasonForSecureAccommodation();
        if (reasonForOrder == ABSCOND) {
            orderGrounds.append("the child has a history of absconding and is likely to abscond from any other ");
            orderGrounds.append("accommodation, and if the child absconds ");
            orderGrounds.append(selectedChildGender.getSubjectPronoun());
            orderGrounds.append(" is likely to suffer significant harm.");
        } else if (reasonForOrder == INJURY) {
            orderGrounds.append("if the child is kept in any other accommodation the child is likely to injure ");
            orderGrounds.append(selectedChildGender.getReflexivePronoun());
            orderGrounds.append(" or other persons.");
        }

        orderGrounds.append(NEW_PARAGRAPH);

        return orderGrounds;
    }

}
