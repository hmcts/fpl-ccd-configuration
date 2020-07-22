package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
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
    private final GeneratedOrderService generatedOrderService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        final EventData eventData = new EventData(orderEvent);

        final String localAuthorityCode = eventData.getLocalAuthorityCode();
        final CaseDetails caseDetails = eventData.getCaseDetails();

        DocumentReference orderDoc = orderEvent.getDocumentReference();
        issuedOrderAdminNotificationHandler.sendToAdmin(
            eventData, orderDoc, GENERATED_ORDER);

        sendNotificationToEmailServedRepresentatives(eventData, orderDoc);
        sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(eventData, orderDoc, localAuthorityCode,
            caseDetails);
    }

    @EventListener
    public void sendNotificationToAllocatedJudgeForOrder(final GeneratedOrderEvent orderEvent) {
        final EventData eventData = new EventData(orderEvent);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        JudgeAndLegalAdvisor mostRecentOrderJudge
            = generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType.GENERATED_ORDER)
            && isNotEmpty(mostRecentOrderJudge.getJudgeEmailAddress())) {
            AllocatedJudgeTemplateForGeneratedOrder parameters = orderIssuedEmailContentProvider
                .buildAllocatedJudgeOrderIssuedNotification(eventData.getCaseDetails());

            String email = mostRecentOrderJudge.getJudgeEmailAddress();

            notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE, email, parameters,
                eventData.getReference());
        }
    }

    private void sendNotificationToEmailServedRepresentatives(final EventData eventData,
                                                              DocumentReference documentReference) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(
                eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentReference,
                GENERATED_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, templateParameters, eventData);
    }

    private void sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(final EventData eventData,
                                                                                 DocumentReference documentReference,
                                                                                 final String localAuthorityCode,
                                                                                 final CaseDetails caseDetails) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithCaseUrl(
                caseDetails, eventData.getLocalAuthorityCode(), documentReference, GENERATED_ORDER);

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
