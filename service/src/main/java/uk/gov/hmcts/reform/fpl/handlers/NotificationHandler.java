package uk.gov.hmcts.reform.fpl.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;
import uk.gov.hmcts.reform.fpl.service.CafcassEmailContentProviderService;
import uk.gov.hmcts.reform.fpl.service.GatekeeperEmailContentProviderService;
import uk.gov.hmcts.reform.fpl.service.HmctsEmailContentProviderService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
public class NotificationHandler {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final HmctsEmailContentProviderService hmctsEmailContentProviderService;
    private final CafcassEmailContentProviderService cafcassEmailContentProviderService;
    private final GatekeeperEmailContentProviderService gatekeeperEmailContentProviderService;
    private final NotificationClient notificationClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationHandler(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                               CafcassLookupConfiguration cafcassLookupConfiguration,
                               NotificationClient notificationClient,
                               HmctsEmailContentProviderService hmctsEmailContentProviderService,
                               CafcassEmailContentProviderService cafcassEmailContentProviderService,
                               GatekeeperEmailContentProviderService gatekeeperEmailContentProviderService) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.notificationClient = notificationClient;
        this.hmctsEmailContentProviderService = hmctsEmailContentProviderService;
        this.cafcassEmailContentProviderService = cafcassEmailContentProviderService;
        this.gatekeeperEmailContentProviderService = gatekeeperEmailContentProviderService;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get("caseLocalAuthority");
        Map<String, String> parameters = hmctsEmailContentProviderService
            .buildHmctsSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();

        sendNotification(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendNotificationToCafcass(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get("caseLocalAuthority");
        Map<String, String> parameters = cafcassEmailContentProviderService
            .buildCafcassSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = (String.valueOf(caseDetails.getId()));
        String email = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        sendNotification(CAFCASS_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendNotificationToGatekeeper(NotifyGatekeeperEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get("caseLocalAuthority");
        String email = (String) caseDetails.getData().get("gateKeeperEmail");
        Map<String, String> parameters = gatekeeperEmailContentProviderService.buildGatekeeperNotification(caseDetails,
            localAuthorityCode);
        String reference = (String.valueOf(caseDetails.getId()));

        sendNotification(GATEKEEPER_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    private void sendNotification(String templateId, String email, Map<String, String> parameters, String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            String message = String.format("Failed to send submission notification (with template id: %s) to %s",
                templateId, email, e);
            throw new AboutToStartOrSubmitCallbackException(message,
                "The email did not send. Try again or come back later.");
        }
    }
}
