package uk.gov.hmcts.reform.fpl.service.orders.generator.common;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderDetailsWithEndTypeGenerator {

    private final ManageOrderDocumentService manageOrderDocumentService;

    public String orderDetails(ManageOrdersEndDateType ordersEndDateType,
                               OrderDetailsWithEndTypeMessages orderDetailsWithEndTypeMessages, CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        Map<String, String> context = manageOrderDocumentService.commonContextElements(caseData);

        LocalDateTime orderExpiration;

        switch (ordersEndDateType) {
            // The DATE_WITH_ORDINAL_SUFFIX format ignores the time, so that it will not display even if captured.
            case CALENDAR_DAY:
                orderExpiration = LocalDateTime.of(eventData.getManageOrdersSetDateEndDate(), LocalTime.MIDNIGHT);
                return getDateTimeAndDateMessage(
                    context,
                    orderExpiration,
                    DATE_WITH_ORDINAL_SUFFIX,
                    orderDetailsWithEndTypeMessages.getMessageWithSpecifiedTime(),
                    getDayOfMonthSuffix(orderExpiration.getDayOfMonth()));
            case CALENDAR_DAY_AND_TIME:
                orderExpiration = eventData.getManageOrdersSetDateAndTimeEndDate();
                return getDateTimeAndDateMessage(
                    context,
                    orderExpiration,
                    DATE_TIME_WITH_ORDINAL_SUFFIX,
                    orderDetailsWithEndTypeMessages.getMessageWithSpecifiedTime(),
                    getDayOfMonthSuffix(orderExpiration.getDayOfMonth()));
            case END_OF_PROCEEDINGS:
                return getEndOfProceedingsMessage(
                    context,
                    orderDetailsWithEndTypeMessages.getMessageWithEndOfProceedings()
                );
            case NUMBER_OF_MONTHS:
                Integer numOfMonths = eventData.getManageOrdersSetMonthsEndDate();
                LocalDate approvalDate = Optional.ofNullable(eventData.getManageOrdersApprovalDateOrDateTime())
                    .map(LocalDateTime::toLocalDate)
                    .orElseThrow();
                orderExpiration = LocalDateTime.of(approvalDate.plusMonths(numOfMonths), LocalTime.MIDNIGHT);
                final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
                return getMonthMessage(
                    context,
                    orderExpiration,
                    DATE_WITH_ORDINAL_SUFFIX,
                    numOfMonths,
                    orderDetailsWithEndTypeMessages.getMessageWithNumberOfMonths(),
                    dayOrdinalSuffix);
            default:
                throw new IllegalStateException("Unexpected order event data type: " + ordersEndDateType);
        }

    }

    private String getMonthMessage(Map<String, String> context, LocalDateTime orderExpiration,
                                   String formatString, Integer numOfMonths,
                                   String courtResponsibilityAssignmentMessage, String dayOrdinalSuffix) {

        context.put("endDate",
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix)));
        context.put("numOfMonths", numOfMonths.toString());
        context.put("decoratedNumberOfMonths", numOfMonths + (numOfMonths > 1 ? " months" : " month"));
        return new StringSubstitutor(context).replace(courtResponsibilityAssignmentMessage);

    }

    private String getEndOfProceedingsMessage(Map<String, String> context,
                                              String courtResponsibilityAssignmentMessage) {
        return new StringSubstitutor(context).replace(courtResponsibilityAssignmentMessage);
    }

    private String getDateTimeAndDateMessage(Map<String, String> context,
                                             LocalDateTime orderExpiration, String formatString,
                                             String courtResponsibilityAssignmentMessage, String dayOrdinalSuffix) {

        context.put("endDate",
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix)));
        return new StringSubstitutor(context).replace(courtResponsibilityAssignmentMessage);

    }

}
