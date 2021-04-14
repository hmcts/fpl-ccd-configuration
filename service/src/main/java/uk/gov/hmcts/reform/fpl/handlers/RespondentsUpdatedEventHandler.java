package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsUpdatedEventHandler {

    private final RespondentSolicitorContentProvider respondentSolicitorContentProvider;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void notifyX(final RespondentsUpdated event) {
        //todo call service from aliveni & soorya
    }

    @Async
    @EventListener
    public void notifyUnregisteredSolicitors(final RespondentsUpdated event) {
        RespondentService respondentService = new RespondentService();

        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> unregisteredSolicitors = new ArrayList<>();
        List<Respondent> respondentsWithLegalRepresentation = respondentService.getRespondentsWithLegalRepresentation(
            caseData.getRespondents1());

        respondentsWithLegalRepresentation.forEach(respondent -> {
            RespondentSolicitor respondentSolicitor = respondent.getSolicitor();
            UnregisteredOrganisation unregisteredOrganisation = respondentSolicitor.getUnregisteredOrganisation();

            if (unregisteredOrganisation != null) {
                if (isNotEmpty(unregisteredOrganisation.getName())
                    || !isEmptyAddress(unregisteredOrganisation.getAddress())) {
                    unregisteredSolicitors.add(respondentSolicitor);
                }
            }
        });

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
