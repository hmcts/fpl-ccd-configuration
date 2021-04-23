package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsSubmittedEventHandler {

    private final RespondentSolicitorContentProvider respondentSolicitorContentProvider;
    private final NotificationService notificationService;
    private final RespondentService respondentService;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> registeredSolicitors = respondentService.getRegisteredSolicitors(
            caseData.getRespondents1());

        notifySolicitors(caseData, registeredSolicitors, REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE);
    }

    @Async
    @EventListener
    public void notifyUnregisteredSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> unregisteredSolicitors = respondentService.getUnregisteredSolicitors(
            caseData.getRespondents1());

        notifySolicitors(caseData, unregisteredSolicitors, UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE);

    }

    private void notifySolicitors(CaseData caseData,
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
