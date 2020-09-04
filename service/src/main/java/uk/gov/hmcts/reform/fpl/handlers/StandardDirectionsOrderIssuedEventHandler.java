package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.StandardDirectionOrderIssuedEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_CTSC_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderIssuedEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyCafcass(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData parameters = cafcassEmailContentProviderSDOIssued
            .getNotifyData(caseData);
        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService
            .sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, recipient, parameters, caseData.getId());
    }

    @EventListener
    public void notifyLocalAuthority(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData);
        String recipient = inboxLookupService.getNotificationRecipientEmail(caseData);

        notificationService
            .sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @EventListener
    public void notifyAllocatedJudge(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)
            && hasJudgeEmail(caseData.getStandardDirectionOrder())) {

            NotifyData notifyData = standardDirectionOrderIssuedEmailContentProvider
                .buildNotificationParametersForAllocatedJudge(caseData);
            String recipient = caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor().getJudgeEmailAddress();

            notificationService
                .sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE, recipient, notifyData, caseData.getId());
        }
    }

    @EventListener
    public void notifyCTSC(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(caseData);
        String recipient = ctscEmailLookupConfiguration.getEmail();

        notificationService
            .sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_CTSC_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    private boolean hasJudgeEmail(StandardDirectionOrder standardDirectionOrder) {
        return isNotEmpty(standardDirectionOrder.getJudgeAndLegalAdvisor())
            && isNotEmpty(standardDirectionOrder.getJudgeAndLegalAdvisor().getJudgeEmailAddress());
    }
}
