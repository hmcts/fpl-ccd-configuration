package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;

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

    @EventListener
    public void notifyParties(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getOrderDocument();

        issuedOrderAdminNotificationHandler.notifyAdmin(caseData, orderDocument.getBinaryUrl(), GENERATED_ORDER);
        sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(caseData, orderDocument.getBinaryUrl());

        sendNotificationToEmailServedRepresentatives(caseData, orderDocument);
    }

    @EventListener
    public void notifyAllocatedJudge(final GeneratedOrderEvent orderEvent) {
        CaseData caseData = orderEvent.getCaseData();

        JudgeAndLegalAdvisor mostRecentOrderJudge
            = generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);

        if (isNotEmpty(mostRecentOrderJudge.getJudgeEmailAddress())) {
            AllocatedJudgeTemplateForGeneratedOrder parameters = orderIssuedEmailContentProvider
                .buildAllocatedJudgeOrderIssuedNotification(caseData);

            String email = mostRecentOrderJudge.getJudgeEmailAddress();

            notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_JUDGE, email, parameters,
                caseData.getId());
        }
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument) {
        final NotifyData notifyData =
            orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(caseData, orderDocument, GENERATED_ORDER);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES, notifyData, caseData);
    }

    private void sendNotificationToLocalAuthorityAndDigitalServedRepresentatives(final CaseData caseData,
                                                                                 final String binaryUrl) {
        final NotifyData notifyData =
            orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData, binaryUrl, GENERATED_ORDER);

        sendToLocalAuthority(caseData, notifyData);
        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, notifyData, caseData);
    }

    private void sendToLocalAuthority(final CaseData caseData,
                                      final NotifyData notifyData) {
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, emails, notifyData,
            caseData.getId().toString());
    }
}
