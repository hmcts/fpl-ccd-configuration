package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43ChildArrangementOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C43ChildArrangementOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private final OrderMessageGenerator orderMessageGenerator;
    @Value("${contacts.passport_office.email}")
    private String passportOfficeEmail;
    @Value("${contacts.passport_office.address}")
    private String passportOfficeAddress;

    public static final String WARNING_MESSAGE = "Where a Child Arrangements Order is in force and the arrangements "
        + "regulated by it consist of, or include, arrangements which relate to either or both (a) with whom the child "
        + "concerned shall live and(b) when the child shall live with any person, no person may cause the child to be "
        + "known by a new surname or remove the child from the United Kingdom without the written consent of every "
        + "person with parental responsibility for the child or the leave of the Court. \n\n"
        + "However, this does not prevent the removal of the child, for a period of less than 1 month, by a person "
        + "named in the Child Arrangements Order as a person with whom the child shall live (Sections13(1), (2) and"
        + " (4) Children Act 1989). \n\n"
        + "It may be a criminal offence under the Child Abduction Act 1984 to remove the child "
        + "from the United Kingdom without the leave of the Court. \n\n"
        + "Where a Child Arrangements Order is in force: if "
        + "you do not comply with a provision of this Child Arrangements Order– \n\n"
        + "(a) you may be held in contempt of Court and be committed to prison or fined; and/or \n"
        + "(b) the Court may make an order requiring you to undertake unpaid work (\"an "
        + "enforcement order\") and/or an order that you pay financial compensation.";

    public static final String CONDITIONS_MESSAGE = "Where - \n"
        + "(a) there are no proceedings pending under Part 2 Children Act 1989; \n"
        + "(b) an officer of the service/ Welsh family proceedings officer who remains involved "
        + "with the case is given cause to suspect, whilst this order is in force, that the "
        + "child concerned is at risk of harm; and \n"
        + "(c) as a result that officer makes a risk assessment under section 16A of that Act, "
        + "the officer may apply to the Court for it to revive the previous proceedings and "
        + "to consider that risk assessment and give such directions as the Court thinks "
        + "necessary.";

    protected static final String NOTICE_MESSAGE = "Any person with "
        + "parental responsibility for a child may obtain advice on what can be done to prevent "
        + "the issue of a passport to the child. They should write to %s or email %s.";

    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final C43ChildArrangementOrderTitleGenerator c43TitleGenerator;

    @Override
    public Order accept() {
        return Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLaOrRelatingLa();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
            c43DocmosisParameters = C43ChildArrangementOrderDocmosisParameters
            .builder()
            .orderTitle(c43TitleGenerator.getOrderTitle(eventData))
            .recitalsOrPreamble(getOrderRecitalsAndPreambles(eventData))
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(buildOrderDetails(eventData))
            .furtherDirections(getOrderDirections(eventData))
            .localAuthorityName(localAuthorityName)
            .noticeHeader("Notice")
            .noticeMessage(String.format(NOTICE_MESSAGE, passportOfficeAddress, passportOfficeEmail));

        if (isChildArrangementOrderSelected(eventData)) {
            addChildArrangementOrderWarningMessage(c43DocmosisParameters);
        }

        return c43DocmosisParameters.build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    private String buildOrderDetails(ManageOrdersEventData eventData) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("The Court orders");

        if (isChildArrangementOrderSelected(eventData)) {
            stringBuilder.append("\n\nThe Child Arrangement Order is for the child to ");
            stringBuilder.append(eventData.getManageOrdersChildArrangementsOrderTypes().stream()
                .map(type -> {
                    switch (type) {
                        case CHILD_LIVE:
                            return "live with";
                        case CHILD_CONTACT:
                            return "have contact with";
                        default: return type.toString();
                    }
                })
                .collect(Collectors.joining(" and ")));
            stringBuilder.append(".");
        }

        return stringBuilder.toString();
    }

    private String getOrderRecitalsAndPreambles(ManageOrdersEventData eventData) {
        return eventData.getManageOrdersRecitalsAndPreambles();
    }

    private String getOrderDirections(ManageOrdersEventData eventData) {
        String directions = eventData.getManageOrdersDirections();
        String furtherDirections = eventData.getManageOrdersFurtherDirections();

        if (!isEmpty(furtherDirections)) {
            directions += "\n\n" + furtherDirections;
        }

        directions += "\n\n" + CONDITIONS_MESSAGE;

        return directions;
    }

    private Boolean isChildArrangementOrderSelected(ManageOrdersEventData eventData) {
        List<C43OrderType> orders = eventData.getManageOrdersMultiSelectListForC43();

        return orders.contains(C43OrderType.CHILD_ARRANGEMENT_ORDER);
    }

    private void addChildArrangementOrderWarningMessage(
        C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
            c43DocmosisParameters) {
        c43DocmosisParameters
            .orderHeader("Warning \n")
            .orderMessage(WARNING_MESSAGE);
    }
}
