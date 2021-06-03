package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C33InterimCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C33InterimCareOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private static final GeneratedOrderType TYPE = GeneratedOrderType.CARE_ORDER;
    private static final String CHILD = "child is";
    private static final String CHILDREN = "children are";

    private final ChildrenService childrenService;
    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    @Override
    public Order accept() {
        return Order.C33_INTERIM_CARE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        return C33InterimCareOrderDocmosisParameters.builder()
            .orderTitle(Order.C33_INTERIM_CARE_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .exclusionClause(eventData.getManageOrdersExclusionDetails())
            .orderDetails(orderDetails(selectedChildren.size(), localAuthorityName, eventData))
            .localAuthorityName(localAuthorityName)
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    private String orderDetails(int numOfChildren, String localAuthorityName, ManageOrdersEventData eventData) {
        LocalDateTime orderExpiration;
        String formatString;
        String childCustodyMessage = "The Court orders that the %s placed in the care of %s until %s.";

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
                childCustodyMessage = "The Court orders that the %s placed in the care of %s until %s.";
                break;
            case END_OF_PROCEEDINGS:
                childCustodyMessage = "The Court orders that the %s placed in the care of %s until "
                    + "the end of the proceedings or until a further order is made.";
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

    private String getEndOfProceedingsMessage(int numOfChildren,
                                              String localAuthorityName,
                                              String courtResponsibilityAssignmentMessage) {
        return String.format(
            courtResponsibilityAssignmentMessage,
            getChildGrammar(numOfChildren),
            localAuthorityName
        );
    }

    private String getDateTimeAndDateMessage(int numOfChildren, String localAuthorityName,
                                             LocalDateTime orderExpiration, String formatString,
                                             String courtResponsibilityAssignmentMessage, String dayOrdinalSuffix) {
        return String.format(
            courtResponsibilityAssignmentMessage,
            getChildGrammar(numOfChildren),
            localAuthorityName,
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix))
        );
    }

    private String getChildGrammar(int numOfChildren) {
        return (numOfChildren == 1) ? CHILD : CHILDREN;
    }
}
