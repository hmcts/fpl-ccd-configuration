package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendedOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;
    private final Time time;
    private final OrderIssuedEmailContentProviderTypeOfOrderCalculator typeCalculator;
    private final EmailNotificationHelper helper;

    public OrderAmendedNotifyData getNotifyData(final CaseData caseData,
                                                final DocumentReference orderDocument,
                                                final IssuedOrderType issuedOrderType) {
        return OrderAmendedNotifyData.builder()
            .lastName(helper.getSubjectLineLastName(caseData))
            .orderType(typeCalculator.getTypeOfOrder(caseData, issuedOrderType))
            .courtName(config.getCourt(caseData.getCaseLocalAuthority()).getName())
            .callout(NOTICE_OF_PLACEMENT_ORDER != issuedOrderType ? buildCalloutWithNextHearing(caseData, time.now()) : "")
            .build();
    }

}
