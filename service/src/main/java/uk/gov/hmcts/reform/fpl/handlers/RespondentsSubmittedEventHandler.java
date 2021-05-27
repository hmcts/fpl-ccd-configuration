package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
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
public class RespondentsSubmittedEventHandler {

    private final RegisteredRespondentSolicitorContentProvider registeredContentProvider;
    private final UnregisteredRespondentSolicitorContentProvider unregisteredContentProvider;
    private final NotificationService notificationService;
    private final RespondentService respondentService;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> solicitors = respondentService.getRegisteredSolicitors(caseData.getRespondents1());

        solicitors.forEach(recipient -> {
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
    public void notifyUnregisteredRespondentSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = unregisteredContentProvider.buildContent(caseData);

        List<RespondentSolicitor> solicitors = respondentService.getUnregisteredSolicitors(caseData.getRespondents1());

        solicitors.forEach(recipient -> notificationService.sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient.getEmail(), notifyData, caseData.getId()
        ));
    }
}
