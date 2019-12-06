package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C21OrderEvent;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CMOOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.content.C21OrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C21_ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;

@Slf4j
@Component
/* preferring this option given growing constructor args */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final C21OrderEmailContentProvider c21OrderEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final NotificationClient notificationClient;
    private final IdamApi idamApi;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

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
    public void sendNotificationForC2Upload(final C2UploadedEvent caseEvent) {
        List<String> roles = idamApi.retrieveUserInfo(caseEvent.getAuthorization()).getRoles();

        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoles())) {
            CaseDetails caseDetailsFromEvent = caseEvent.getCallbackRequest().getCaseDetails();
            String localAuthorityCode = (String) caseDetailsFromEvent.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);

            Map<String, Object> parameters = c2UploadedEmailContentProvider.buildC2UploadNotification(
                caseDetailsFromEvent);
            String reference = Long.toString(caseDetailsFromEvent.getId());

            String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();
            sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE, email, parameters, reference);
        }
    }

    @EventListener
    public void sendNotificationForC21Order(final C21OrderEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);

        sendC21NotificationForLocalAuthority(caseDetails, localAuthorityCode, event.getMostRecentUploadedDocumentUrl());
        sendC21NotificationForCafcass(caseDetails, localAuthorityCode, event.getMostRecentUploadedDocumentUrl());
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
    public void notifyCafcassOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = cafcassEmailContentProviderSDOIssued
            .buildCafcassStandardDirectionOrderIssuedNotification(caseDetails, localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());
        String email = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();
        sendNotification(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void notifyLocalAuthorityOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);
        sendNotification(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters, reference);
    }

    @EventListener
    public void notifyLocalAuthorityOfIssuedAndServedCaseManagementOrder(CMOOrderEvent cmoOrderEvent) {
        CaseDetails caseDetails = cmoOrderEvent.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = caseManagementOrderEmailContentProvider
            .buildCMOOrderIssuedNotificationParametersForLocalAuthority(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);
        sendNotification(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, email, parameters, reference);
    }

    private void sendNotification(String templateId, String email, Map<String, Object> parameters, String reference) {
        log.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send submission notification (with template id: {}) to {}", templateId, email, e);
        }
    }

    private void sendC21NotificationForCafcass(final CaseDetails caseDetails, final String localAuthorityCode,
                                               final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> cafCassParameters =
            c21OrderEmailContentProvider.buildC21OrderNotificationParametersForCafcass(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);
        String cafcassEmail = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();
        sendNotification(C21_ORDER_NOTIFICATION_TEMPLATE, cafcassEmail, cafCassParameters,
            Long.toString(caseDetails.getId()));
    }

    private void sendC21NotificationForLocalAuthority(final CaseDetails caseDetails, final String localAuthorityCode,
                                                      final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> localAuthorityParameters =
            c21OrderEmailContentProvider.buildC21OrderNotificationParametersForLocalAuthority(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);
        String localAuthorityEmail = localAuthorityEmailLookupConfiguration
            .getLocalAuthority(localAuthorityCode)
            .map(LocalAuthorityEmailLookupConfiguration.LocalAuthority::getEmail)
            .orElseThrow(() -> new NullPointerException("Local authority '" + localAuthorityCode + "' not found"));
        sendNotification(C21_ORDER_NOTIFICATION_TEMPLATE, localAuthorityEmail, localAuthorityParameters,
            Long.toString(caseDetails.getId()));
    }
}
