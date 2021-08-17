package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C29ActionsPermitted;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.PlacedUnderOrder;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C29RecoveryOfAChildDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C29RecoveryOfAChildDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    private static final String paragraphBreak = "\n\n";
    private static final String ORDER_HEADER = "Warning\n";
    private static final String ORDER_MESSAGE = "It is an offence intentionally to obstruct the "
        + "person from removing the child (Section 50(9) Children Act 1989).";
    private boolean displayWarningMessage = false;
    private boolean displayAddress = false;

    @Override
    public Order accept() {
        return Order.C29_RECOVERY_OF_A_CHILD;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);
        String dayOrdinalSuffix = getDayOfMonthSuffix(eventData.getManageOrdersOrderCreatedDate().getDayOfMonth());

        final String orderMadeDate = formatLocalDateToString(
            eventData.getManageOrdersOrderCreatedDate(),
            String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix));
        return C29RecoveryOfAChildDocmosisParameters.builder()
            .orderTitle(Order.C29_RECOVERY_OF_A_CHILD.getTitle())
            .dateOfIssue(orderMadeDate)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(getOrderDetails(eventData, selectedChildren.size(), orderMadeDate, localAuthorityName))
            .localAuthorityName(localAuthorityName)
            .orderHeader(getOrderHeader())
            .orderMessage(getOrderMessage())
            .build();
    }

    private String getOrderDetails(ManageOrdersEventData eventData, int numOfChildren,
                                   String orderMadeDate, String localAuthorityName) {
        String childOrChildren = (numOfChildren == 1 ? "child" : "children");
        String officerReference = getOfficerReferenceMessage(eventData);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getStandardMessage(localAuthorityName, childOrChildren, orderMadeDate, eventData));

        if (eventData.getManageOrdersActionsPermitted().contains(C29ActionsPermitted.ENTRY)) {
            displayAddress = true;
            stringBuilder.append(getEntryMessage(officerReference,
                formatAddress(eventData.getManageOrdersActionsPermittedAddress()),
                childOrChildren));
        }
        if (eventData.getManageOrdersActionsPermitted().contains(C29ActionsPermitted.INFORM)) {
            stringBuilder.append(getInformMessage(officerReference));
        }
        if (eventData.getManageOrdersActionsPermitted().contains(C29ActionsPermitted.PRODUCE)) {
            stringBuilder.append(getProduceMessage(officerReference));
        }
        if (eventData.getManageOrdersActionsPermitted().contains(C29ActionsPermitted.REMOVE)) {
            stringBuilder.append(getRemoveMessage(officerReference));
            displayWarningMessage = true;
            getOrderMessage();
        }

        stringBuilder.append(getIsExparteMessage(eventData));


        return stringBuilder.toString();
    }

    private String getStandardMessage(String localAuthorityName, String childOrChildren,
                                      String orderMadeDate, ManageOrdersEventData eventData) {

        PlacedUnderOrder order = eventData.getManageOrdersPlacedUnderOrder();

        return format("The Court is satisfied that %s "
                + "has parental responsibility for the %s "
                + "by virtue of a %s "
                + "made on %s.%s",
            localAuthorityName,
            childOrChildren,
            order.getLabel(),
            orderMadeDate,
            paragraphBreak);
    }

    private String getEntryMessage(String officerReference, String address, String childOrChildren) {
        return format("The court authorises %s to enter "
                + "the premises known as %s, and search for the %s, using reasonable force if necessary.%s",
            officerReference, address, childOrChildren, paragraphBreak);
    }

    private String getInformMessage(String officerReference) {
        return format("The court requires any person who has information about where the child is,"
            + " or may be, to give that information to %s or an officer of the court, if asked to do so.%s",
        officerReference, paragraphBreak);
    }

    private String getProduceMessage(String officerReference) {
        return format("The court directs that any person who can produce the child when asked to by "
            + "%s to do so.%s", officerReference, paragraphBreak);
    }

    private String getRemoveMessage(String officerReference) {
        return format("The Court authorises %s to remove the child.%s", officerReference, paragraphBreak);
    }

    private String getIsExparteMessage(ManageOrdersEventData eventData) {
        return eventData.getManageOrdersIsExParte().equals("Yes")
            ? format("This order has been made exparte.", paragraphBreak)
            : format("This order has not been made exparte.", paragraphBreak);
    }

    private String getOrderMessage() {
        return displayWarningMessage ? ORDER_MESSAGE : null;
    }

    private String getOrderHeader() {
        return displayWarningMessage ? ORDER_HEADER : null;
    }

    private String getOfficerReferenceMessage(ManageOrdersEventData eventData) {
        return isNull(eventData.getManageOrdersOfficerName())
            ? "a police constable"
            : eventData.getManageOrdersOfficerName();
    }

    private String formatAddress(Address removalAddress) {
        return Optional.ofNullable(removalAddress)
            .map(address -> address.getAddressAsString(", ")).orElse("");
    }
}
