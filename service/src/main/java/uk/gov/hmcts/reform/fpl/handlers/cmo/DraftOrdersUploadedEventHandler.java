package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftOrdersUploadedContentProvider;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersUploadedEventHandler {

    private final NotificationService notificationService;
    private final DraftOrdersUploadedContentProvider contentProvider;

    @Async
    @EventListener
    public void sendNotificationToJudge(final DraftOrdersUploaded event) {
        final CaseData caseData = event.getCaseData();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        final List<HearingOrder> orders = nullSafeList(caseData.getHearingOrdersBundlesDrafts()).stream()
            .map(Element::getValue)
            .filter(b -> Objects.equals(b.getHearingId(), caseData.getLastHearingOrderDraftsHearingId()))
            .findFirst()
            .map(HearingOrdersBundle::getOrders)
            .map(ElementUtils::unwrapElements)
            .orElse(emptyList());

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

        final DraftOrdersUploadedTemplate content = contentProvider.buildContent(caseData, hearing, judge, orders);

        notificationService.sendEmail(
            DRAFT_ORDERS_UPLOADED_NOTIFICATION_TEMPLATE,
            judge.getJudgeEmailAddress(),
            content,
            caseData.getId().toString()
        );

    }
}
