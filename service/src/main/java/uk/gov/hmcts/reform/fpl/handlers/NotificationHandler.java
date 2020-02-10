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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
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
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Slf4j
@Component
/* preferring this option given growing constructor args */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final GeneratedOrderEmailContentProvider orderEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final NotificationClient notificationClient;
    private final IdamApi idamApi;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;
    private final RepresentativeService representativeService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper objectMapper;

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());
        String email = hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();

        sendNotification(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void sendNotificationForC2Upload(final C2UploadedEvent event) {
        List<String> roles = idamApi.retrieveUserInfo(event.getAuthorization()).getRoles();
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoles())) {
            EventData eventData = new EventData(event);
            Map<String, Object> parameters = c2UploadedEmailContentProvider.buildC2UploadNotification(
                eventData.getCaseDetails());
            String email = hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();

            sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE, email, parameters, eventData.getReference());
        }
    }

    @EventListener
    public void sendNotificationForOrder(final GeneratedOrderEvent event) {
        EventData eventData = new EventData(event);

        sendOrderNotificationForLocalAuthority(eventData.getCaseDetails(), eventData.getLocalAuthorityCode(),
            event.getMostRecentUploadedDocumentUrl());
    }

    @EventListener
    public void sendNotificationToCafcass(SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = cafcassEmailContentProvider
            .buildCafcassSubmissionNotification(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        sendNotification(CAFCASS_SUBMISSION_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void sendNotificationToGatekeeper(NotifyGatekeeperEvent event) {
        EventData eventData = new EventData(event);
        String email = (String) eventData.getCaseDetails().getData().get("gateKeeperEmail");
        Map<String, Object> parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        sendNotification(GATEKEEPER_SUBMISSION_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void notifyCafcassOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = cafcassEmailContentProviderSDOIssued
            .buildCafcassStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        sendNotification(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void notifyLocalAuthorityOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        sendNotification(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void sendNotificationsForIssuedCaseManagementOrder(final CaseManagementOrderIssuedEvent event) {
        EventData eventData = new EventData(event);

        sendCMOCaseLinkNotifications(eventData);
        sendCMODocumentLinkNotifications(eventData, event.getDocumentContents());
    }

    private void sendCMOCaseLinkNotifications(final EventData eventData) {
        sendCMOCaseLinkNotificationForLocalAuthority(eventData);
        sendCMOCaseLinkNotificationToRepresentatives(eventData);
    }

    @EventListener
    public void sendNotificationForCaseManagementOrderReadyForJudgeReview(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> parameters = caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(eventData.getCaseDetails());

        String email = hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();

        sendNotification(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, email, parameters, eventData.getReference());
    }

    @EventListener
    public void sendNotificationToPartiesAddedToCase(PartyAddedToCaseEvent event) {
        List<Element<Representative>> representatives = event.getRepresentativesToNotify();
        EventData eventData = new EventData(event);

        if (isNotEmpty(representatives)) {
            representatives.stream().forEach(representativeElement -> {
                String emailForRepresentative = representativeElement.getValue().getEmail();
                RepresentativeServingPreferences servingPreferencesForRep
                        = representativeElement.getValue().getServingPreferences();
                if (!servingPreferencesForRep.equals(POST)) {

                    Map<String, Object> parameters = partyAddedToCaseContentProvider
                        .getPartyAddedToCaseNotificationParameters(event.getCallbackRequest().getCaseDetails(),
                           servingPreferencesForRep);
                    String notificationTemplate = partyAddedToCaseContentProvider
                        .getPartyAddedToCaseNotificationTemplate(servingPreferencesForRep);

                    sendNotification(notificationTemplate, emailForRepresentative, parameters,
                        eventData.getReference());
                }
            });
        }
    }

    private void sendCMOCaseLinkNotificationForLocalAuthority(final EventData eventData) {
        final String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(
            eventData.getLocalAuthorityCode());

        Map<String, Object> localAuthorityNotificationParameters = caseManagementOrderEmailContentProvider
            .buildCMOIssuedCaseLinkNotificationParameters(eventData.getCaseDetails(), localAuthorityName);

        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        sendNotification(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, email, localAuthorityNotificationParameters,
            eventData.getReference());
    }

    private void sendCMOCaseLinkNotificationToRepresentatives(final EventData eventData) {
        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), DIGITAL_SERVICE);

        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> {
                Map<String, Object> representativeNotificationParameters =
                    caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(
                        eventData.getCaseDetails(), representative.getFullName());

                sendNotification(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, representative.getEmail(),
                    representativeNotificationParameters, eventData.getReference());
            });
    }

    private void sendCMODocumentLinkNotifications(final EventData eventData, final byte[] documentContents) {
        sendCMODocumentLinkNotificationForCafcass(eventData, documentContents);
        sendCMODocumentLinkNotificationsToRepresentatives(eventData, documentContents);
    }

    private void sendCMODocumentLinkNotificationForCafcass(final EventData eventData, final byte[] documentContents) {
        final String cafcassName = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getName();

        Map<String, Object> cafcassParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
                eventData.getCaseDetails(), cafcassName, documentContents);

        String cafcassEmail = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        sendNotification(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE, cafcassEmail, cafcassParameters,
            eventData.getReference());
    }

    private void sendCMODocumentLinkNotificationsToRepresentatives(final EventData eventData,
                                                                   final byte[] documentContents) {
        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), EMAIL);

        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> {
                Map<String, Object> representativeNotificationParameters =
                    caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
                        eventData.getCaseDetails(), representative.getFullName(), documentContents);

                sendNotification(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE, representative.getEmail(),
                    representativeNotificationParameters, eventData.getReference());
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

    private void sendOrderNotificationForLocalAuthority(final CaseDetails caseDetails, final String localAuthorityCode,
                                                        final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> localAuthorityParameters =
            orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);

        sendNotification(ORDER_NOTIFICATION_TEMPLATE, recipientEmail, localAuthorityParameters,
            Long.toString(caseDetails.getId()));
    }

    @Getter
    private static class EventData {
        private CaseDetails caseDetails;
        private String localAuthorityCode;
        private String reference;

        private EventData(CallbackEvent event) {
            this.caseDetails = event.getCallbackRequest().getCaseDetails();
            this.localAuthorityCode = (String) this.caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
            this.reference = Long.toString(this.caseDetails.getId());
        }
    }
}
