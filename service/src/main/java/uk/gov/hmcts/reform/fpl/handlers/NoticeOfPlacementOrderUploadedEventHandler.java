package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfPlacementOrderUploadedEventHandler {
    private final ObjectMapper objectMapper;
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final RepresentativeService representativeService;
    private final RepresentativeNotificationService representativeNotificationService;
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

        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        List<Representative> representativesServedByDigitalService =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), DIGITAL_SERVICE);
        List<Representative> representativesServedByEmail =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL);

        representativeNotificationService.sendNotificationToRepresentatives(eventData, parameters,
            representativesServedByDigitalService, NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE);

        representativeNotificationHandler.sendOrderIssuedNotificationToRepresentatives(eventData,
            noticeOfPlacementEvent.getDocumentContents(), representativesServedByEmail, NOTICE_OF_PLACEMENT_ORDER);

    }
}
