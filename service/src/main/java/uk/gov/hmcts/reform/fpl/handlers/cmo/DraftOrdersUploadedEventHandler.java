package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersUploadedEventHandler {

    private final NotificationService notificationService;
    private final DraftOrdersUploadedContentProvider draftOrdersContentProvider;
    private final AgreedCMOUploadedContentProvider agreedCMOContentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;

    @Async
    @EventListener
    public void sendNotificationToJudge(final DraftOrdersUploaded event) {
        final CaseData caseData = event.getCaseData();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        final List<HearingOrder> orders = getOrders(caseData);

        if (isEmpty(orders)) {
            return;
        }

        AbstractJudge judge = null;

        if (hearing != null && hearing.getJudgeAndLegalAdvisor() != null
            && isNotEmpty(hearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress())) {
            judge = hearing.getJudgeAndLegalAdvisor();
        } else if (caseData.hasAllocatedJudgeEmail()) {
            judge = caseData.getAllocatedJudge();
        }

        if (judge == null || isEmpty(judge.getJudgeEmailAddress())) {
            return;
        }

        final DraftOrdersUploadedTemplate content = draftOrdersContentProvider.buildContent(caseData, hearing, judge,
            orders);

        notificationService.sendEmail(
            DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            content,
            caseData.getId().toString()
        );
    }

    @Async
    @EventListener
    public void sendNotificationForAdmin(final AgreedCMOUploaded event) {
        CaseData caseData = event.getCaseData();

        final List<HearingOrder> orders = getOrders(caseData);

        if (isEmpty(orders) || !orders.get(0).getType().equals(AGREED_CMO)) {
            return;
        }

        JudgeAndLegalAdvisor judgeAttendingHearing = event.getHearing().getJudgeAndLegalAdvisor();

        if (judgeAttendingHearing.getJudgeEmailAddress() != null || caseData.hasAllocatedJudgeEmail()) {
            AbstractJudge judge = getJudgeToNotify(judgeAttendingHearing, caseData.getAllocatedJudge());

            CMOReadyToSealTemplate template = agreedCMOContentProvider.buildTemplate(
                event.getHearing(),
                caseData.getId(),
                judge,
                caseData.getAllRespondents(),
                caseData.getFamilyManCaseNumber()
            );

            String email = adminNotificationHandler.getHmctsAdminEmail(caseData);

            notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
                email,
                template,
                caseData.getId().toString());
        }
    }

    private List<HearingOrder> getOrders(CaseData caseData) {
        return nullSafeList(caseData.getHearingOrdersBundlesDrafts()).stream()
            .map(Element::getValue)
            .filter(b -> Objects.equals(b.getHearingId(), caseData.getLastHearingOrderDraftsHearingId()))
            .findFirst()
            .map(HearingOrdersBundle::getOrders)
            .map(ElementUtils::unwrapElements)
            .orElse(emptyList());
    }

    private AbstractJudge getJudgeToNotify(JudgeAndLegalAdvisor judgeAttendingHearing, Judge allocatedJudge) {
        return judgeAttendingHearing.getJudgeEmailAddress() != null ? judgeAttendingHearing : allocatedJudge;
    }
}
