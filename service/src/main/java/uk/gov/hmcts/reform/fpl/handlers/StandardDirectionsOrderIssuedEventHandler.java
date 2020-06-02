package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.StandardDirectionOrderIssuedEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderIssuedEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyCafcassOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = cafcassEmailContentProviderSDOIssued
            .buildCafcassStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        notificationService.sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyLocalAuthorityOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        if (featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)) {
            EventData eventData = new EventData(event);
            CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
            AllocatedJudgeTemplateForSDO parameters = standardDirectionOrderIssuedEmailContentProvider
                .buildNotificationParametersForAllocatedJudge(eventData.getCaseDetails());

            String email = caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor().getJudgeEmailAddress();

            notificationService.sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_JUDGE_TEMPLATE, email, parameters,
                eventData.getReference());
        }
    }
}
