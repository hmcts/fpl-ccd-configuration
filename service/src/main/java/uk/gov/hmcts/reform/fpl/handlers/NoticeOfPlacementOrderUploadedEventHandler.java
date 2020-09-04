package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfPlacementOrderUploadedEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @EventListener
    public void notifyParties(NoticeOfPlacementOrderUploadedEvent noticeOfPlacementEvent) {
        CaseData caseData = noticeOfPlacementEvent.getCaseData();

        String recipient = inboxLookupService.getNotificationRecipientEmail(caseData);
        NotifyData notifyData =
            localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(caseData);

        notificationService
            .sendEmail(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, recipient, notifyData, caseData.getId());

        issuedOrderAdminNotificationHandler.notifyAdmin(caseData,
            noticeOfPlacementEvent.getDocumentContents(), NOTICE_OF_PLACEMENT_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, notifyData, caseData);

        OrderIssuedNotifyData representativesTemplateParameters =
            orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(caseData,
                noticeOfPlacementEvent.getDocumentContents(), NOTICE_OF_PLACEMENT_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, representativesTemplateParameters, caseData);
    }
}
