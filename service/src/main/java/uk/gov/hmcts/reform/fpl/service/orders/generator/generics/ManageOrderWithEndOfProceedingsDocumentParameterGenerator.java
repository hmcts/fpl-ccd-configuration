package uk.gov.hmcts.reform.fpl.service.orders.generator.generics;

import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisParameterGenerator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

public abstract class ManageOrderWithEndOfProceedingsDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenService childrenService;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    private static final String CHILD = "child";
    private static final String CHILDREN = "children";

    protected ManageOrderWithEndOfProceedingsDocumentParameterGenerator(
        ChildrenService childrenService, LocalAuthorityNameLookupConfiguration laNameLookup) {

        this.childrenService = childrenService;
        this.laNameLookup = laNameLookup;
    }

    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);


        return docmosisParameters(eventData,localAuthorityCode, localAuthorityName, selectedChildren);
    }

    protected abstract DocmosisParameters docmosisParameters(ManageOrdersEventData eventData, String localAuthorityCode,
                                                    String localAuthorityName, List<Element<Child>> selectedChildren);

    protected String orderDetails(int numOfChildren, String localAuthorityName, ManageOrdersEventData eventData) {
        LocalDateTime orderExpiration;
        String formatString;
        String childCustodyMessage = "The Court orders %s supervises the %s until %s.";

        ManageOrdersEndDateType type = eventData.getManageOrdersEndDateTypeWithEndOfProceedings();
        switch (type) {
            // The DATE_WITH_ORDINAL_SUFFIX format ignores the time, so that it will not display even if captured.
            case CALENDAR_DAY:
                formatString = DATE_WITH_ORDINAL_SUFFIX;
                orderExpiration = LocalDateTime.of(eventData.getManageOrdersSetDateEndDate(), LocalTime.MIDNIGHT);
                break;
            case CALENDAR_DAY_AND_TIME:
                formatString = DATE_TIME_WITH_ORDINAL_SUFFIX;
                orderExpiration = eventData.getManageOrdersSetDateAndTimeEndDate();
                break;
            case END_OF_PROCEEDINGS:
                childCustodyMessage = "The Court orders %s supervises the %s until "
                    + "the end of the proceedings or further order.";
                formatString = null;
                orderExpiration = null;
                break;
            default:
                throw new IllegalStateException("Unexpected order event data type: " + type);
        }

        if (type == END_OF_PROCEEDINGS) {
            return getEndOfProceedingsMessage(
                numOfChildren,
                localAuthorityName,
                childCustodyMessage
            );
        } else {
            final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
            return getDateTimeAndDateMessage(
                numOfChildren, localAuthorityName,
                orderExpiration,
                formatString,
                childCustodyMessage,
                dayOrdinalSuffix);
        }
    }

    private String getEndOfProceedingsMessage(int numOfChildren, String localAuthorityName,
                                              String courtResponsibilityAssignmentMessage) {
        return String.format(
            courtResponsibilityAssignmentMessage,
            localAuthorityName,
            getChildGrammar(numOfChildren)
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
