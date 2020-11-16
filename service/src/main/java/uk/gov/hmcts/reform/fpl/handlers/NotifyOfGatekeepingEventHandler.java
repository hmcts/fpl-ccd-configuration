package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.NotifyOfGatekeeingEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyOfGatekeepingEventHandler {
    private final NotificationService notificationService;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendEmailToGatekeeper(NotifyOfGatekeeingEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyGatekeeperTemplate parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData);

        List<String> emailList = getDistinctGatekeeperEmails(caseData.getGatekeeperEmails());

        emailList.forEach(recipientEmail -> {
            NotifyGatekeeperTemplate template = parameters.duplicate();

            template.setGatekeeperRecipients(gatekeeperEmailContentProvider.buildRecipientsLabel(
                emailList, recipientEmail));

            notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, recipientEmail, template,
                caseData.getId().toString());
        });
    }

    @EventListener
    public void sendEmailToLocalCourt(NotifyOfGatekeeingEvent event) {
        CaseData caseData = event.getCaseData();
        NotifyGatekeeperTemplate parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData);

        String localAuthority = caseData.getCaseLocalAuthority();

        if (localAuthority != null && featureToggleService.isNotifyCourtOfGatekeepingEnabled(localAuthority)) {
            String email = hmctsCourtLookupConfiguration.getCourt(localAuthority).getEmail();

            notificationService.sendEmail(GATEKEEPER_SUBMISSION_COURT_TEMPLATE, email, parameters,
                caseData.getId().toString());
        }
    }

    private List<String> getDistinctGatekeeperEmails(List<Element<EmailAddress>> emailCollection) {
        return unwrapElements(emailCollection)
            .stream()
            .distinct()
            .map(EmailAddress::getEmail)
            .collect(Collectors.toList());
    }
}
