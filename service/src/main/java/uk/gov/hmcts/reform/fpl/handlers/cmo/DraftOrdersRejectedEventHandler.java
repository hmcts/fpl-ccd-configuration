package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersRejectedEventHandler {

    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;

    @Async
    @EventListener
    public void sendNotificationToLA(final DraftOrdersRejected event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> rejectedOrders = event.getRejectedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final RejectedOrdersTemplate content = contentProvider.buildOrdersRejectedContent(
            caseData, hearing, rejectedOrders
        );

        notificationService.sendEmail(JUDGE_REJECTS_DRAFT_ORDERS, recipients, content, caseData.getId());
    }
}
