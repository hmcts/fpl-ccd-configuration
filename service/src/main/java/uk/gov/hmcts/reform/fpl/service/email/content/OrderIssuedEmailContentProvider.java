package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
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
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final CourtService courtService;
    private final Time time;
    private final OrderIssuedEmailContentProviderTypeOfOrderCalculator typeCalculator;
    private final EmailNotificationHelper helper;

    public OrderIssuedNotifyData getNotifyDataWithoutCaseUrl(final CaseData caseData,
                                                             final DocumentReference orderDocument,
                                                             final IssuedOrderType issuedOrderType) {
        return commonOrderIssuedNotifyData(caseData, issuedOrderType).toBuilder()
            .documentLink(linkToAttachedDocument(orderDocument))
            .build();
    }

    public OrderIssuedNotifyData getNotifyDataWithCaseUrl(final CaseData caseData,
                                                          final DocumentReference orderDocument,
                                                          final IssuedOrderType issuedOrderType) {
        if (issuedOrderType == CMO) {
            return getNotifyDataForCMO(caseData, orderDocument, issuedOrderType);
        } else {
            return commonOrderIssuedNotifyData(caseData, issuedOrderType).toBuilder()
                .documentLink(getDocumentUrl(orderDocument))
                .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
                .build();
        }
    }

    public OrderIssuedNotifyData getNotifyDataForCMO(final CaseData caseData,
                                                     final DocumentReference orderDocument,
                                                     final IssuedOrderType issuedOrderType) {
        UUID hearingId = caseData.getLastHearingOrderDraftsHearingId();
        HearingBooking hearing = findElement(hearingId, caseData.getAllHearings())
            .orElseThrow(() -> new HearingNotFoundException("No hearing found with id: " + hearingId))
            .getValue();

        return commonOrderIssuedNotifyData(caseData, issuedOrderType).toBuilder()
            .documentLink(getDocumentUrl(orderDocument))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .callout("^" + buildSubjectLineWithHearingBookingDateSuffix(
                caseData.getFamilyManCaseNumber(), caseData.getRespondents1(), hearing))
            .build();
    }

    private OrderIssuedNotifyData commonOrderIssuedNotifyData(final CaseData caseData,
                                                              final IssuedOrderType type) {
        return OrderIssuedNotifyData.builder()
            .lastName(helper.getSubjectLineLastName(caseData))
            .orderType(typeCalculator.getTypeOfOrder(caseData, type))
            .courtName(courtService.getCourtName(caseData))
            .callout(NOTICE_OF_PLACEMENT_ORDER != type ? buildCalloutWithNextHearing(caseData, time.now()) : "")
            .build();
    }

}
