package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsUpdatedEventHandler {

    private final RespondentSolicitorContentProvider respondentSolicitorContentProvider;
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

        notifyUpdatedSolicitors(caseData, registeredSolicitors, REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE);
    }

    @Async
    @EventListener
    public void notifyUnregisteredSolicitors(final RespondentsUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<RespondentSolicitor> unregisteredSolicitors = respondentService.getUnregisteredSolicitors(
            caseData.getRespondents1());

        List<RespondentSolicitor> unregisteredSolicitorsBefore = respondentService.getUnregisteredSolicitors(
            caseDataBefore.getRespondents1());

        unregisteredSolicitors.removeAll(unregisteredSolicitorsBefore);

        notifyUpdatedSolicitors(caseData, unregisteredSolicitors, UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE);

    }

    private void notifyUpdatedSolicitors(CaseData caseData,
                                         List<RespondentSolicitor> updatedSolicitors,
                                         String solicitorEmailTemplate) {
        updatedSolicitors.forEach(recipient -> {
            RespondentSolicitorTemplate notifyData =
                respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(caseData, recipient);

            notificationService.sendEmail(
                solicitorEmailTemplate,
                recipient.getEmail(),
                notifyData,
                caseData.getId());
        });
    }
}
