package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Component
/* preferring this option given growing constructor args */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final GeneratedOrderEmailContentProvider orderEmailContentProvider;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final InboxLookupService inboxLookupService;
    private final RepresentativeService representativeService;
    private final ObjectMapper objectMapper;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final NotificationService notificationService;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        EventData eventData = new EventData(orderEvent);

        sendOrderNotificationToLocalAuthority(eventData.getCaseDetails(), eventData.getLocalAuthorityCode(),
            orderEvent.getMostRecentUploadedDocumentUrl());
        sendOrderIssuedNotificationToAdmin(eventData, orderEvent.getDocumentContents(), GENERATED_ORDER);

        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representativesServedByEmail = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), EMAIL);

        sendOrderIssuedNotificationToRepresentatives(eventData, orderEvent.getDocumentContents(),
            representativesServedByEmail, GENERATED_ORDER);
    }

    @EventListener
    public void sendEmailForNoticeOfPlacementOrderUploaded(
        NoticeOfPlacementOrderUploadedEvent noticeOfPlacementEvent) {
        EventData eventData = new EventData(noticeOfPlacementEvent);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        Map<String, Object> parameters =
            localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(eventData.caseDetails);

        notificationService.sendEmail(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, recipientEmail, parameters,
            eventData.reference);
        sendOrderIssuedNotificationToAdmin(eventData, noticeOfPlacementEvent.getDocumentContents(),
            NOTICE_OF_PLACEMENT_ORDER);

        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        List<Representative> representativesServedByDigitalService =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), DIGITAL_SERVICE);
        List<Representative> representativesServedByEmail =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL);

        sendNotificationToRepresentatives(eventData, parameters, representativesServedByDigitalService,
            NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE);

        sendOrderIssuedNotificationToRepresentatives(eventData, noticeOfPlacementEvent.getDocumentContents(),
            representativesServedByEmail, NOTICE_OF_PLACEMENT_ORDER);

    }

    private void sendNotificationToRepresentatives(EventData eventData,
                                                   Map<String, Object> parameters,
                                                   List<Representative> representatives,
                                                   String templateId) {
        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> notificationService.sendEmail(
                templateId,
                representative.getEmail(),
                parameters,
                eventData.getReference()));
    }

    private void sendOrderNotificationToLocalAuthority(final CaseDetails caseDetails,
                                                       final String localAuthorityCode,
                                                       final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> localAuthorityParameters =
            orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);

        notificationService.sendEmail(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA, recipientEmail,
            localAuthorityParameters, Long.toString(caseDetails.getId()));
    }

    private void sendOrderIssuedNotificationToAdmin(final EventData eventData,
                                                    final byte[] documentContents,
                                                    final IssuedOrderType issuedOrderType) {
        Map<String, Object> parameters = orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, issuedOrderType);

        String email = getHmctsAdminEmail(eventData);

        notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN, email, parameters,
            Long.toString(eventData.getCaseDetails().getId()));
    }

    private void sendOrderIssuedNotificationToRepresentatives(final EventData eventData,
                                                              final byte[] documentContents,
                                                              final List<Representative> representatives,
                                                              final IssuedOrderType issuedOrderType) {
        if (!representatives.isEmpty()) {
            Map<String, Object> parameters =
                orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
                    eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, issuedOrderType);

            sendNotificationToRepresentatives(eventData, parameters, representatives,
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES);
        } else {
            log.debug("No notification sent to representatives (none require serving)");
        }
    }

    private String getHmctsAdminEmail(EventData eventData) {
        String ctscValue = getCtscValue(eventData.getCaseDetails().getData());

        if (ctscValue.equals("Yes")) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();
    }

    private String getCtscValue(Map<String, Object> caseData) {
        return caseData.get("sendToCtsc") != null ? caseData.get("sendToCtsc").toString() : "No";
    }

    @Getter
    private static class EventData {
        private CaseDetails caseDetails;
        private String localAuthorityCode;
        private String reference;

        private EventData(CallbackEvent event) {
            this.caseDetails = event.getCallbackRequest().getCaseDetails();
            this.localAuthorityCode = (String) this.caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
            this.reference = Long.toString(this.caseDetails.getId());
        }
    }
}
