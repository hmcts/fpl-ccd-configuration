package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.CafcassSubmissionTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.HmctsSubmissionTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmittedCaseEventHandler {
    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;

    @EventListener
    public void sendEmailToHmctsAdmin(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE,
            adminNotificationHandler.getHmctsAdminEmail(eventData),
            buildEmailTemplatePersonalisationForLocalAuthority(eventData),
            eventData.getReference());
    }

    @EventListener
    public void sendEmailToCafcass(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE,
            getEmailRecipientForCafcass(eventData.getLocalAuthorityCode()),
            buildEmailTemplatePersonalisationForCafcass(eventData),
            eventData.getReference());
    }

    private HmctsSubmissionTemplate buildEmailTemplatePersonalisationForLocalAuthority(final EventData eventData) {
        return hmctsEmailContentProvider.buildHmctsSubmissionNotification(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());
    }

    private String getEmailRecipientForCafcass(final String localAuthority) {
        return cafcassLookupConfiguration.getCafcass(localAuthority).getEmail();
    }

    private CafcassSubmissionTemplate buildEmailTemplatePersonalisationForCafcass(final EventData eventData) {
        return cafcassEmailContentProvider.buildCafcassSubmissionNotification(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());
    }
}
