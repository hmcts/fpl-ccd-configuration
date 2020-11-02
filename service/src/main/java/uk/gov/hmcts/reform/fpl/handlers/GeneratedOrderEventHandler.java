package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
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
    private final GeneratedOrderService generatedOrderService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final byte[] documentContents = orderEvent.getDocumentContents();

        issuedOrderAdminNotificationHandler.sendToAdmin(caseData, orderEvent.getDocumentContents(), GENERATED_ORDER);

        sendNotificationToEmailServedRepresentatives(caseData, documentContents);
        sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(caseData, documentContents);
    }

    @EventListener
    public void sendNotificationToAllocatedJudgeForOrder(final GeneratedOrderEvent orderEvent) {
        CaseData caseData = orderEvent.getCaseData();

        JudgeAndLegalAdvisor mostRecentOrderJudge
            = generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType.GENERATED_ORDER)
            && isNotEmpty(mostRecentOrderJudge.getJudgeEmailAddress())) {
            AllocatedJudgeTemplateForGeneratedOrder parameters = orderIssuedEmailContentProvider
                .buildAllocatedJudgeOrderIssuedNotification(caseData);

            String email = mostRecentOrderJudge.getJudgeEmailAddress();

            notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE, email, parameters,
                caseData.getId().toString());
        }
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final byte[] documentContents) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithoutCaseUrl(caseData, documentContents, GENERATED_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, templateParameters, caseData);
    }

    private void sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(final CaseData caseData,
                                                                                 final byte[] documentContents) {
        final Map<String, Object> templateParameters =
            orderIssuedEmailContentProvider.buildParametersWithCaseUrl(caseData, documentContents, GENERATED_ORDER);

        sendToLocalAuthority(caseData, templateParameters);
        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, templateParameters, caseData);
    }

    private void sendToLocalAuthority(final CaseData caseData,
                                      final Map<String, Object> templateParameters) {
        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        notificationService.sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, emails, templateParameters,
            caseData.getId().toString());
    }
}
