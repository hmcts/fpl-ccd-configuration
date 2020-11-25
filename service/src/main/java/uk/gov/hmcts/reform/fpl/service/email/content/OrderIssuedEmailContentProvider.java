package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;
    private final GeneratedOrderService generatedOrderService;
    private final Time time;

    public OrderIssuedNotifyData getNotifyDataWithoutCaseUrl(final CaseData caseData,
                                                             final byte[] documentContents,
                                                             final IssuedOrderType issuedOrderType) {
        return OrderIssuedNotifyData.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .orderType(getTypeOfOrder(caseData, issuedOrderType))
            .courtName(config.getCourt(caseData.getCaseLocalAuthority()).getName())
            .callout((issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) ? buildCallout(caseData) : "")
            .documentLink(generateAttachedDocumentLink(documentContents).orElse(null))
            .build();
    }

    public OrderIssuedNotifyData getNotifyDataWithCaseUrl(final CaseData caseData,
                                                          final byte[] documentContents,
                                                          final IssuedOrderType issuedOrderType) {
        return getNotifyDataWithoutCaseUrl(caseData, documentContents, issuedOrderType)
            .toBuilder()
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .build();
    }

    public AllocatedJudgeTemplateForGeneratedOrder buildAllocatedJudgeOrderIssuedNotification(CaseData caseData) {

        JudgeAndLegalAdvisor judge = getAllocatedJudge(caseData);

        return AllocatedJudgeTemplateForGeneratedOrder.builder()
            .orderType(getTypeOfOrder(caseData, GENERATED_ORDER))
            .callout(buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .respondentLastName(getFirstRespondentLastName(caseData))
            .judgeTitle(judge.getJudgeOrMagistrateTitle())
            .judgeName(judge.getJudgeName())
            .build();
    }

    private JudgeAndLegalAdvisor getAllocatedJudge(CaseData caseData) {
        return generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);
    }

    private String buildCallout(final CaseData caseData) {
        HearingBooking hearing = null;
        if (caseData.hasFutureHearing(caseData.getHearingDetails())) {
            hearing = caseData.getMostUrgentHearingBookingAfter(time.now());
        }
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearing);
    }

    private String getTypeOfOrder(CaseData caseData, IssuedOrderType issuedOrderType) {
        String orderType;
        if (issuedOrderType == GENERATED_ORDER) {
            orderType = Iterables.getLast(caseData.getOrderCollection()).getValue().getType();
        } else {
            orderType = issuedOrderType.getLabel();
        }

        return orderType.toLowerCase();
    }


}
