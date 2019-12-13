package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CMOEvent;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

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
    private final GeneratedOrderEmailContentProvider orderEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final NotificationClient notificationClient;
    private final IdamApi idamApi;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;
    private final RepresentativeService representativeService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper objectMapper;

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
    public void sendNotificationForOrder(final GeneratedOrderEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);

        sendOrderNotificationForLocalAuthority(caseDetails, localAuthorityCode,
            event.getMostRecentUploadedDocumentUrl());
        sendOrderNotificationForCafcass(caseDetails, localAuthorityCode, event.getMostRecentUploadedDocumentUrl());
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
    public void sendNotificationsForIssuedCaseManagementOrder(CMOEvent event) {
        EventData eventData = new EventData(event);

        sendCMOCaseLinkNotifications(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());
    }

    private void sendCMOCaseLinkNotificationForLocalAuthority(final CaseDetails caseDetails,
                                                              final String localAuthorityCode) {
        final String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(
            localAuthorityCode);

        Map<String, Object> localAuthorityNotificationParameters = caseManagementOrderEmailContentProvider
            .buildCMOIssuedCaseLinkNotificationParameters(caseDetails, localAuthorityName);

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);
        sendNotification(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, email,
            localAuthorityNotificationParameters, Long.toString(caseDetails.getId()));
    }

    private void sendCMOCaseLinkNotifications(final CaseDetails caseDetails, final String localAuthorityCode) {
        sendCMOCaseLinkNotificationForLocalAuthority(caseDetails, localAuthorityCode);
        sendCMOCaseLinkNotificationToRepresentatives(caseDetails);
    }

    private void sendCMOCaseLinkNotificationToRepresentatives(final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), DIGITAL_SERVICE);

        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> {
                Map<String, Object> representativeNotificationParameters =
                    caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(
                        caseDetails, representative.getFullName());

                sendNotification(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, representative.getEmail(),
                    representativeNotificationParameters, Long.toString(caseDetails.getId()));
            });
    }

    private void sendNotification(String templateId, String email, Map<String, Object> parameters, String reference) {
        log.debug("Sending submission notification (with template id: {}) to {}", templateId, email);
        try {
            notificationClient.sendEmail(templateId, email, parameters, reference);
        } catch (NotificationClientException e) {
            log.error("Failed to send submission notification (with template id: {}) to {}", templateId, email, e);
        }
    }

    private void sendOrderNotificationForCafcass(final CaseDetails caseDetails, final String localAuthorityCode,
                                                 final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> cafCassParameters =
            orderEmailContentProvider.buildOrderNotificationParametersForCafcass(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);
        String cafcassEmail = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();
        sendNotification(ORDER_NOTIFICATION_TEMPLATE, cafcassEmail, cafCassParameters,
            Long.toString(caseDetails.getId()));
    }

    private void sendOrderNotificationForLocalAuthority(final CaseDetails caseDetails, final String localAuthorityCode,
                                                        final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> localAuthorityParameters =
            orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);
        String localAuthorityEmail = localAuthorityEmailLookupConfiguration
            .getLocalAuthority(localAuthorityCode)
            .map(LocalAuthorityEmailLookupConfiguration.LocalAuthority::getEmail)
            .orElseThrow(() -> new NullPointerException("Local authority '" + localAuthorityCode + "' not found"));
        sendNotification(ORDER_NOTIFICATION_TEMPLATE, localAuthorityEmail, localAuthorityParameters,
            Long.toString(caseDetails.getId()));
    }

    private void sendCMODocumentLinkNotificationForCafcass(final CaseDetails caseDetails,
                                                           final String localAuthorityCode,
                                                           final DocmosisDocument document) {
        Map<String, Object> cafcassParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParametersForCafcass(caseDetails,
                localAuthorityCode, document);

        String cafcassEmail = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        sendNotification(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE, cafcassEmail, cafcassParameters,
            Long.toString(caseDetails.getId()));
    }

    @Getter
    static class EventData {
        private final CaseDetails caseDetails;
        private final String localAuthorityCode;
        private final String caseReference;

        public EventData(CallbackEvent event) {
            this.caseDetails = event.getCallbackRequest().getCaseDetails();
            this.localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
            this.caseReference = Long.toString(caseDetails.getId());
        }
    }
}
