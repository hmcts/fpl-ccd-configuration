package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsUpdatedEventHandler {

    private final RespondentSolicitorContentProvider respondentSolicitorContentProvider;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final SubmittedCaseEvent event) {
        RespondentService respondentService = new RespondentService();

        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> registeredSolicitors = respondentService.getRegisteredSolicitors(
            caseData.getRespondents1());

        registeredSolicitors.forEach(registeredSolicitor -> {
            RespondentSolicitorTemplate notifyData = respondentSolicitorContentProvider
                .buildRespondentSolicitorSubmissionNotification(caseData, registeredSolicitor);

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
                registeredSolicitor.getEmail(),
                notifyData,
                caseData.getId()
            );
        });
    }

    @Async
    @EventListener
    public void notifyUnregisteredSolicitors(final RespondentsUpdated event) {
        RespondentService respondentService = new RespondentService();

        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> unregisteredSolicitors = respondentService.getUnregisteredSolicitors(
            caseData.getRespondents1());

        unregisteredSolicitors.forEach(recipient -> {
            RespondentSolicitorTemplate notifyData =
                respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(caseData, recipient);

            notificationService.sendEmail(
                UNREGISTERED_RESPONDENT_SOLICICTOR,
                recipient.getEmail(),
                notifyData,
                caseData.getId());
        });
    }
}
