package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C35aSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrderEndDateTypeWithMonth.SET_NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C35aSupervisionOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private static final GeneratedOrderType TYPE = GeneratedOrderType.SUPERVISION_ORDER;
    private static final String CHILD = "child";
    private static final String CHILDREN = "children";

    private final ChildrenService childrenService;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    @Override
    public Order accept() {
        return Order.C35A_SUPERVISION_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C35aSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C35A_SUPERVISION_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName, eventData))
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    private String orderDetails(int numOfChildren, String localAuthorityName, ManageOrdersEventData eventData) {
        LocalDateTime orderExpiration;
        String formatString;
        Integer numOfMonths = null;
        String courtResponsibilityAssignmentMessage = "The Court orders %s supervises the %s until %s.";

        switch (eventData.getManageOrdersEndDateTypeWithMonth()) {
            // The DATE_WITH_ORDINAL_SUFFIX format ignores the time, so that it will not display even if captured.
            case SET_CALENDAR_DAY:
                formatString = DATE_WITH_ORDINAL_SUFFIX;
                orderExpiration = LocalDateTime.of(eventData.getManageOrdersSetDateEndDate(), LocalTime.MIDNIGHT);
                break;
            case SET_CALENDAR_DAY_AND_TIME:
                formatString = DATE_TIME_WITH_ORDINAL_SUFFIX;
                orderExpiration = eventData.getManageOrdersSetDateAndTimeEndDate();
                break;
            case SET_NUMBER_OF_MONTHS:
                formatString = DATE_WITH_ORDINAL_SUFFIX;
                LocalDate approvalDate = eventData.getManageOrdersApprovalDate();
                numOfMonths = eventData.getManageOrdersSetMonthsEndDate();
                orderExpiration = LocalDateTime.of(approvalDate.plusMonths(numOfMonths), LocalTime.MIDNIGHT);
                courtResponsibilityAssignmentMessage =
                    "The Court orders %s supervises the %s for %s months from the date of this order until %s.";
                break;
            default:
                throw new IllegalStateException("Unexpected supervision order event data type: "
                    + eventData.getManageOrdersEndDateTypeWithMonth());
        }

        final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
        boolean isMonthOptionSelected = eventData.getManageOrdersEndDateTypeWithMonth().equals(SET_NUMBER_OF_MONTHS);

        if (isMonthOptionSelected) {
            return getMonthMessage(
                numOfChildren,
                localAuthorityName,
                orderExpiration,
                formatString,
                numOfMonths,
                courtResponsibilityAssignmentMessage,
                dayOrdinalSuffix);
        } else {
            return getDateTimeAndDateMessage(
                numOfChildren, localAuthorityName,
                orderExpiration,
                formatString,
                courtResponsibilityAssignmentMessage,
                dayOrdinalSuffix);
        }
    }

    private String getMonthMessage(int numOfChildren, String localAuthorityName, LocalDateTime orderExpiration,
                                   String formatString, Integer numOfMonths,
                                   String courtResponsibilityAssignmentMessage, String dayOrdinalSuffix) {
        return String.format(
            courtResponsibilityAssignmentMessage,
            localAuthorityName,
            getChildGrammar(numOfChildren),
            numOfMonths,
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix))
        );
    }

    private String getDateTimeAndDateMessage(int numOfChildren, String localAuthorityName,
                                             LocalDateTime orderExpiration, String formatString,
                                             String courtResponsibilityAssignmentMessage, String dayOrdinalSuffix) {
        return String.format(
            courtResponsibilityAssignmentMessage,
            localAuthorityName,
            getChildGrammar(numOfChildren),
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix))
        );
    }

    private String getChildGrammar(int numOfChildren) {
        return (numOfChildren == 1) ? CHILD : CHILDREN;
    }
}
