package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32bDischargeOfCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C32bDischargeOfCareOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final LocalAuthorityNameLookupConfiguration laNameLookup;

    private final HearingVenueLookUpService hearingVenueLookUpService;

    @Override
    public Order accept() {
        return Order.C32B_DISCHARGE_OF_CARE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);


        HearingVenue hearingVenue =
            hearingVenueLookUpService.getHearingVenue(eventData.getManageOrdersCareOrderIssuedCourt());

        return C32bDischargeOfCareOrderDocmosisParameters.builder()
            .orderTitle(Order.C32B_DISCHARGE_OF_CARE_ORDER.getTitle())
            .orderDetails(orderDetails(eventData, hearingVenue))
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .localAuthorityName(localAuthorityName)
            .showCareRestrictions(true)
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    private String orderDetails(ManageOrdersEventData eventData, HearingVenue hearingVenue) {

        String dischargeMessage = "The Court discharges the care order made by %s "
            + "made on %s.";

        String issuedCourt = hearingVenue.getVenue();
        String issuedDate = getIssuedDate(eventData);

        return String.format(dischargeMessage, issuedCourt, issuedDate);
    }

    public String getIssuedDate(ManageOrdersEventData eventData) {
        LocalDateTime issuedDate = LocalDateTime.of(eventData.getManageOrdersCareOrderIssuedDate(), LocalTime.MIDNIGHT);

        String ordinalSuffix = getDayOfMonthSuffix(issuedDate.getDayOfMonth());
        String formatString = formatLocalDateTimeBaseUsingFormat(issuedDate, DATE_WITH_ORDINAL_SUFFIX);

        return String.format(formatString, ordinalSuffix);
    }
}
