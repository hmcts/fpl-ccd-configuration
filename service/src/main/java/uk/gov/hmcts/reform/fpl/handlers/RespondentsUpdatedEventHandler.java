package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.cafcass.ChangeOfAddressData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CHANGE_OF_ADDRESS;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsUpdatedEventHandler {

    private final RegisteredRepresentativeSolicitorContentProvider registeredContentProvider;
    private final UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider;
    private final NotificationService notificationService;
    private final RespondentService respondentService;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @Async
    @EventListener
    public void notifyChangeOfAddress(final RespondentsUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
            cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent() && respondentService.hasAddressChange(caseData.getAllRespondents(),
            caseDataBefore.getAllRespondents())) {
            cafcassNotificationService.sendEmail(caseData, CHANGE_OF_ADDRESS,
                ChangeOfAddressData.builder().build());
        }
    }

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
            NotifyData notifyData = registeredContentProvider.buildContent(caseData, recipient);

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
