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
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.StandardDirectionOrderIssuedEmailContentProvider;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_JUDGE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
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

    // TODO - add ticket number
    // Needs refactored to use NotifyObject rather than Map<String, Object>
    @EventListener
    public void notifyCafcassOfIssuedSDOAndNoticeOfProceedings(StandardDirectionsOrderIssuedEvent event) {
        String notifyTemplate = STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;

        if (featureToggleService.isSendNoticeOfProceedingsFromSdo()) {
            notifyTemplate = SDO_AND_NOP_ISSUED_CAFCASS;
        }

        CaseData caseData = event.getCaseData();
        Map<String, Object> parameters = cafcassEmailContentProviderSDOIssued
            .buildCafcassStandardDirectionOrderIssuedNotification(caseData);
        String email = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(notifyTemplate, email, parameters, caseData.getId().toString());
    }

    // TODO - add ticket number
    // Needs refactored to use NotifyObject rather than Map<String, Object>
    @EventListener
    public void notifyLocalAuthorityOfIssuedSDOAndNoticeOfProceedings(StandardDirectionsOrderIssuedEvent event) {
        String notifyTemplate = STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;

        if (featureToggleService.isSendNoticeOfProceedingsFromSdo()) {
            notifyTemplate = SDO_AND_NOP_ISSUED_LA;
        }

        CaseData caseData = event.getCaseData();
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseData);
        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        notificationService.sendEmail(notifyTemplate, emails, parameters, caseData.getId().toString());
    }

    @EventListener
    public void notifyAllocatedJudgeOfIssuedSDOandNoticeOfProceedings(StandardDirectionsOrderIssuedEvent event) {
        String notifyTemplate = STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE;

        if (featureToggleService.isSendNoticeOfProceedingsFromSdo()) {
            notifyTemplate = SDO_AND_NOP_ISSUED_JUDGE;
        }

        CaseData caseData = event.getCaseData();

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)
            && hasJudgeEmail(caseData.getStandardDirectionOrder())) {
            AllocatedJudgeTemplateForSDO parameters = standardDirectionOrderIssuedEmailContentProvider
                .buildNotificationParametersForAllocatedJudge(caseData);

            String email = caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor().getJudgeEmailAddress();

            notificationService.sendEmail(notifyTemplate, email, parameters, caseData.getId().toString());
        }
    }

    @EventListener
    public void notifyCTSCOfIssuedSDOandNoticeOfProceedings(StandardDirectionsOrderIssuedEvent event) {
        String notifyTemplate = STANDARD_DIRECTION_ORDER_ISSUED_CTSC_TEMPLATE;

        if (featureToggleService.isSendNoticeOfProceedingsFromSdo()) {
            notifyTemplate = SDO_AND_NOP_ISSUED_CTSC;
        }

        CaseData caseData = event.getCaseData();
        CTSCTemplateForSDO parameters = standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(caseData);
        String email = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(notifyTemplate, email, parameters, caseData.getId().toString());
    }

    private boolean hasJudgeEmail(StandardDirectionOrder standardDirectionOrder) {
        return isNotEmpty(standardDirectionOrder.getJudgeAndLegalAdvisor())
            && isNotEmpty(standardDirectionOrder.getJudgeAndLegalAdvisor().getJudgeEmailAddress());
    }
}
