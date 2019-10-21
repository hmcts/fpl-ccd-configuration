package uk.gov.hmcts.reform.fpl.handlers;

import com.microsoft.applicationinsights.boot.dependencies.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.SDOSubmittedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.*;

@Component
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final NotificationClient notificationClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public NotificationHandler(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                               CafcassLookupConfiguration cafcassLookupConfiguration,
                               LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                               NotificationClient notificationClient,
                               HmctsEmailContentProvider hmctsEmailContentProvider,
                               CafcassEmailContentProvider cafcassEmailContentProvider,
                               GatekeeperEmailContentProvider gatekeeperEmailContentProvider, LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.notificationClient = notificationClient;
        this.hmctsEmailContentProvider = hmctsEmailContentProvider;
        this.cafcassEmailContentProvider = cafcassEmailContentProvider;
        this.gatekeeperEmailContentProvider = gatekeeperEmailContentProvider;
        this.localAuthorityEmailContentProvider = localAuthorityEmailContentProvider;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();

        sendNotification(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendNotificationToCafcass(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = cafcassEmailContentProvider
            .buildCafcassSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());
        String email = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        sendNotification(CAFCASS_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendNotificationToGatekeeper(NotifyGatekeeperEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        String email = (String) caseDetails.getData().get("gateKeeperEmail");
        Map<String, Object> parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(caseDetails,
            localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());

        sendNotification(GATEKEEPER_SUBMISSION_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendSDOToCafcass(SDOSubmittedEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = cafcassEmailContentProvider
            .buildCafcassSDOSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());
        String email = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();
        sendNotification(SDO_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void sendSDOToLocalAuthority(SDOSubmittedEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthoritySDOSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail();
        sendNotification(SDO_TEMPLATE, email, parameters, reference);
    }

    private void sendNotification(String templateId, String email, Map<String, Object> parameters, String reference) {
        logger.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.error("Failed to send submission notification (with template id: {}) to {}", templateId, email, e);
        }
    }
}
