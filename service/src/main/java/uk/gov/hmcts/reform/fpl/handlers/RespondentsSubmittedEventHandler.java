package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsSubmittedEventHandler {

    private final RegisteredRepresentativeSolicitorContentProvider registeredContentProvider;
    private final UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider;
    private final NotificationService notificationService;
    private final RespondentService respondentService;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        List<Respondent> respondentsWithRegisteredSolicitors
            = respondentService.getRespondentsWithRegisteredSolicitors(caseData.getRespondents1());

        respondentsWithRegisteredSolicitors.forEach(respondent -> {
            NotifyData notifyData = registeredContentProvider.buildContent(
                caseData, respondent);

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                respondent.getSolicitor().getEmail(),
                notifyData, caseData.getId());
        });
    }

    @Async
    @EventListener
    public void notifyUnregisteredRespondentSolicitors(final RespondentsSubmitted event) {
        CaseData caseData = event.getCaseData();

        List<Respondent> respondents = respondentService.getRespondentsWithUnregisteredSolicitors(
            defaultIfNull(caseData.getRespondents1(), new ArrayList<>()));

        respondents.forEach(respondent -> {
            NotifyData notifyData = unregisteredContentProvider.buildContent(caseData, respondent);
            notificationService.sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                respondent.getSolicitor().getEmail(),
                notifyData, caseData.getId());
        });
    }
}
