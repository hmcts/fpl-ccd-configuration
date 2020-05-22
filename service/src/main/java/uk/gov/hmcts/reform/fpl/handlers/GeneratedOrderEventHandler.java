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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.*;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final ObjectMapper mapper;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        final EventData eventData = new EventData(orderEvent);

        final String localAuthorityCode = eventData.getLocalAuthorityCode();
        final CaseDetails caseDetails = eventData.getCaseDetails();
        final byte[] documentContents = orderEvent.getDocumentContents();

        issuedOrderAdminNotificationHandler.sendToAdmin(
            eventData, orderEvent.getDocumentContents(), GENERATED_ORDER);

        sendNotificationToEmailServedRepresentatives(eventData, documentContents);
        sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(eventData, documentContents, localAuthorityCode,
            caseDetails);
    }

    @EventListener
    public void sendNotificationToAllocatedJudgeForOrder(final GeneratedOrderEvent orderEvent) {
        final EventData eventData = new EventData(orderEvent);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        Map<String, Object> parameters = orderIssuedEmailContentProvider
            .buildAllocatedJudgeOrderIssuedNotification(eventData.getCaseDetails());

        String email = getAllocatedJudgeEmail(caseData);

        notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE, email, parameters,
            eventData.getReference());
    }

    private String getAllocatedJudgeEmail(CaseData caseData) {
        Optional<Element<GeneratedOrder>> order = caseData.getOrderCollection().stream().reduce((first, last) -> last);
        System.out.println("Name is" + order.get().getValue().getJudgeAndLegalAdvisor().getJudgeOrMagistrateTitle()
            + order.get().getValue().getJudgeAndLegalAdvisor().getJudgeName());
        System.out.println("Email address is" + order.get().getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress());
        //add email box for orders
        return  order.get().getValue().getJudgeAndLegalAdvisor().getJudgeEmailAddress();
    }

    private void sendNotificationToEmailServedRepresentatives(final EventData eventData,
                                                              final byte[] documentContents) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
                eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, GENERATED_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, templateParameters, eventData);
    }

    private void sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(final EventData eventData,
                                                                               final byte[] documentContents,
                                                                               final String localAuthorityCode,
                                                                               final CaseDetails caseDetails) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
                caseDetails, eventData.getLocalAuthorityCode(), documentContents, GENERATED_ORDER);

        sendToLocalAuthority(caseDetails, localAuthorityCode, templateParameters);
        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, templateParameters, eventData);
    }

    private void sendToLocalAuthority(final CaseDetails caseDetails, final String localAuthorityCode,
                                      final Map<String, Object> templateParameters) {
        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);

        notificationService.sendEmail(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES,
            recipientEmail, templateParameters, Long.toString(caseDetails.getId()));
    }
}
