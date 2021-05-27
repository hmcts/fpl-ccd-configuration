package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
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

        List<RespondentSolicitor> registeredSolicitors = respondentService.getRegisteredSolicitors(
            caseData.getRespondents1());

        List<RespondentSolicitor> registeredSolicitorsBefore = respondentService.getRegisteredSolicitors(
            caseDataBefore.getRespondents1());

        registeredSolicitors.removeAll(registeredSolicitorsBefore);

        registeredSolicitors.forEach(recipient -> {
            NotifyData notifyData = registeredContentProvider.buildRespondentSolicitorSubmissionNotification(
                caseData, recipient
            );

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient.getEmail(), notifyData, caseData.getId()
            );
        });
    }

    @Async
    @EventListener
    public void notifyUnregisteredRespondentSolicitors(final RespondentsUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        NotifyData notifyData = unregisteredContentProvider.buildContent(caseData);

        List<RespondentSolicitor> unregisteredSolicitors = respondentService.getUnregisteredSolicitors(
            caseData.getRespondents1());

        List<RespondentSolicitor> unregisteredSolicitorsBefore = respondentService.getUnregisteredSolicitors(
            caseDataBefore.getRespondents1());

        unregisteredSolicitors.removeAll(unregisteredSolicitorsBefore);

        unregisteredSolicitors.forEach(recipient -> notificationService.sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient.getEmail(),
            notifyData, caseData.getId()
        ));
    }

}
