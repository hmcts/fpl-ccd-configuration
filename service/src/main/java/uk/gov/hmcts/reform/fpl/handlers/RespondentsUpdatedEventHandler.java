package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor.RegisteredRespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor.UnregisteredRespondentSolicitorContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsUpdatedEventHandler {

    private final RegisteredRespondentSolicitorContentProvider registeredContentProvider;
    private final UnregisteredRespondentSolicitorContentProvider unregisteredContentProvider;
    private final NotificationService notificationService;
    private final RespondentService respondentService;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final RespondentsUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<Respondent> respondentsWithRegisteredSolicitors
            = respondentService.getRespondentsWithRegisteredSolicitors(caseData.getRespondents1());

        List<Respondent> respondentsWithRegisteredSolicitorsBefore
            = respondentService.getRespondentsWithRegisteredSolicitors(caseDataBefore.getRespondents1());

        respondentsWithRegisteredSolicitors.removeAll(respondentsWithRegisteredSolicitorsBefore);

        respondentsWithRegisteredSolicitors.forEach(recipient -> {
            NotifyData notifyData = registeredContentProvider.buildRespondentSolicitorSubmissionNotification(
                caseData, recipient
            );

            notificationService.sendEmail(REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                recipient.getSolicitor().getEmail(), notifyData, caseData.getId()
            );
        });
    }

    @Async
    @EventListener
    public void notifyUnregisteredRespondentSolicitors(final RespondentsUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<Respondent> respondentsWithUnregisteredSolicitors
            = respondentService.getRespondentsWithUnregisteredSolicitors(caseData.getRespondents1());

        List<Respondent> respondentsWithUnregisteredSolicitorsBefore
            = respondentService.getRespondentsWithUnregisteredSolicitors(caseDataBefore.getRespondents1());

        respondentsWithUnregisteredSolicitors.removeAll(respondentsWithUnregisteredSolicitorsBefore);

        respondentsWithUnregisteredSolicitors.forEach(recipient -> {
            NotifyData notifyData = unregisteredContentProvider.buildContent(caseData, recipient);
            notificationService.sendEmail(UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                recipient.getSolicitor().getEmail(), notifyData, caseData.getId());
        });
    }

}
