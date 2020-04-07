package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderEventHandler {
    private final ObjectMapper objectMapper;
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final RepresentativeService representativeService;
    private final GeneratedOrderEmailContentProvider orderEmailContentProvider;
    private final RepresentativeNotificationHandler representativeNotificationHandler;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        EventData eventData = new EventData(orderEvent);

        issuedOrderAdminNotificationHandler.sendToAdmin(eventData,
            orderEvent.getDocumentContents(), GENERATED_ORDER);

        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representativesServedByEmail = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), EMAIL);

        representativeNotificationHandler.sendOrderIssuedNotificationToRepresentatives(eventData,
            orderEvent.getDocumentContents(), representativesServedByEmail, GENERATED_ORDER);

        notifyAllOthers(eventData, orderEvent.getDocumentContents());
    }

    private void sendOrderNotificationToLocalAuthority(final CaseDetails caseDetails,
                                                       final String localAuthorityCode,
                                                       final Map<String, Object> templateParameters) {
        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);

        notificationService.sendEmail(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            recipientEmail, templateParameters, Long.toString(caseDetails.getId()));
    }

    private void notifyAllOthers(final EventData eventData,
                                 final byte[] documentContents) {
        final String localAuthorityCode = eventData.getLocalAuthorityCode();
        final CaseDetails caseDetails = eventData.getCaseDetails();
        final Map<String, Object> templateParameters = orderEmailContentProvider.buildOrderNotificationParameters(
                caseDetails, eventData.getLocalAuthorityCode(), documentContents);

        sendOrderNotificationToLocalAuthority(caseDetails, localAuthorityCode, templateParameters);
        representativeNotificationHandler.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, templateParameters, eventData);
    }
}
