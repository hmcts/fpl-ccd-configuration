package uk.gov.hmcts.reform.fpl.service.orders.generator.commons;

import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.CALENDAR_DAY_AND_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

public class ManageOrdersHelper {

    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final LocalDateTime NEXT_WEEK_DATE_TIME = time.now().plusDays(7);
    private static final Child CHILD = mock(Child.class);
    private static final String CHILD_GRAMMAR = "child";
    private static final String CHILDREN_GRAMMAR = "children";
    private static final String FURTHER_DIRECTIONS = "Further directions ";
    private String dayOrdinalSuffix;
    private String courtOrderMessage;


    public static String getChildSupervisionMessageWithDate(String laName, int numOfChildren, boolean isDateTime,
                                                            ManageOrdersEventData eventData) {
        String formatString;
        LocalDateTime orderExpiration;

        if (isDateTime) {
            formatString = DATE_TIME_WITH_ORDINAL_SUFFIX;
            orderExpiration = eventData.getManageOrdersSetDateAndTimeEndDate();
        } else {
            formatString = DATE_WITH_ORDINAL_SUFFIX;
            orderExpiration = LocalDateTime.of(eventData.getManageOrdersSetDateEndDate(), LocalTime.MIDNIGHT);
        }

        final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
        String courtMessage = "The Court orders %s supervises the %s until %s.";

        return String.format(
            courtMessage,
            laName,
            getChildGrammar(numOfChildren),
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix))
        );
    }

    public static String getChildSupervisionMessageWithMonths(String laName, int numOfChildren,
                                                              int numOfMonths, String formattedDate) {
        return "The Court orders " + laName
            + " supervises the " + getChildGrammar(numOfChildren)
            + " for " + numOfMonths + " months from the date of this order"
            + " until " + formattedDate + ".";
    }

    public static String getChildSupervisionMessageWithEndOfProceedings(String laName, int numOfChildren) {
        return "The Court orders " + laName
            + " supervises the " + getChildGrammar(numOfChildren)
            + " until the end of the proceedings or further order.";
    }

    private static String getChildGrammar(int numOfChildren) {
        return (numOfChildren == 1) ? CHILD_GRAMMAR : CHILDREN_GRAMMAR;
    }

    public static CaseData buildCaseDataWithCalendarDaySpecified(String laCode) {
        return CaseData.builder()
            .caseLocalAuthority(laCode)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY)
                .manageOrdersSetDateEndDate(NEXT_WEEK_DATE_TIME.toLocalDate())
                .build())
            .build();
    }

    public static CaseData buildCaseDataWithDateTimeSpecified(String laCode) {
        return CaseData.builder()
            .caseLocalAuthority(laCode)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(CALENDAR_DAY_AND_TIME)
                .manageOrdersSetDateAndTimeEndDate(NEXT_WEEK_DATE_TIME)
                .build())
            .build();
    }

    public static CaseData buildCaseData(String laCode, ManageOrdersEndDateType type) {
        return CaseData.builder()
            .caseLocalAuthority(laCode)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .manageOrdersEndDateTypeWithEndOfProceedings(type)
                .manageOrdersSetDateAndTimeEndDate(NEXT_WEEK_DATE_TIME)
                .build())
            .build();
    }
}
