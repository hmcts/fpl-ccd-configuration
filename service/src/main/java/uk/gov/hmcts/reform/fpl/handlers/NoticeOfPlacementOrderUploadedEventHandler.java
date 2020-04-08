package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;

import java.util.Map;

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
    private final RepresentativeNotificationHandler representativeNotificationHandler;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @EventListener
    public void sendEmailForNoticeOfPlacementOrderUploaded(NoticeOfPlacementOrderUploadedEvent noticeOfPlacementEvent) {
        EventData eventData = new EventData(noticeOfPlacementEvent);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        Map<String, Object> parameters =
            localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(
                eventData.getCaseDetails());

        notificationService.sendEmail(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, recipientEmail, parameters,
            eventData.getReference());
        issuedOrderAdminNotificationHandler.sendToAdmin(eventData,
            noticeOfPlacementEvent.getDocumentContents(), NOTICE_OF_PLACEMENT_ORDER);

        representativeNotificationHandler.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, parameters, eventData);

        Map<String, Object> representativesTemplateParameters =
            orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
                eventData.getCaseDetails(), eventData.getLocalAuthorityCode(),
                noticeOfPlacementEvent.getDocumentContents(), NOTICE_OF_PLACEMENT_ORDER);

        representativeNotificationHandler.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, representativesTemplateParameters, eventData);
    }
}
