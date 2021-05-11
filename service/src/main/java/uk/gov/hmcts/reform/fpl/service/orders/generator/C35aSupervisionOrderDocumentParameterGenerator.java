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

        switch (eventData.getManageSupervisionOrderEndDateType()) {
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
                int numOfMonths = eventData.getManageOrdersSetMonthsEndDate();
                orderExpiration = LocalDateTime.of(approvalDate.plusMonths(numOfMonths), LocalTime.MIDNIGHT);
                break;
            default:
                throw new IllegalStateException("Unexpected supervision order event data type: "
                    + eventData.getManageSupervisionOrderEndDateType());
        }

        final String dayOrdinalSuffix = getDayOfMonthSuffix(orderExpiration.getDayOfMonth());
        return String.format(
            "It is ordered that %s supervises the %s until %s.",
            localAuthorityName,
            (numOfChildren == 1) ? CHILD : CHILDREN,
            formatLocalDateTimeBaseUsingFormat(orderExpiration, String.format(formatString, dayOrdinalSuffix))
        );
    }
}
