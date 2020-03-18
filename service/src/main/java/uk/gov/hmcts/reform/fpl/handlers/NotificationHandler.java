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
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Component
/* preferring this option given growing constructor args */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;
    private final CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final GeneratedOrderEmailContentProvider orderEmailContentProvider;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;
    private final InboxLookupService inboxLookupService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;
    private final PlacementApplicationContentProvider placementApplicationContentProvider;
    private final RepresentativeService representativeService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper objectMapper;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final NotificationService notificationService;

    @EventListener
    public void sendEmailsForOrder(final GeneratedOrderEvent orderEvent) {
        EventData eventData = new EventData(orderEvent);

        sendOrderNotificationToLocalAuthority(eventData.getCaseDetails(), eventData.getLocalAuthorityCode(),
            orderEvent.getMostRecentUploadedDocumentUrl());

        sendOrderIssuedNotificationToAdmin(eventData, orderEvent.getDocumentContents(), GENERATED_ORDER);
    }

    @EventListener
    public void sendEmailToGatekeeper(NotifyGatekeeperEvent event) {
        EventData eventData = new EventData(event);
        String email = (String) eventData.getCaseDetails().getData().get("gateKeeperEmail");
        Map<String, Object> parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyCafcassOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = cafcassEmailContentProviderSDOIssued
            .buildCafcassStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        notificationService.sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyLocalAuthorityOfIssuedStandardDirectionsOrder(StandardDirectionsOrderIssuedEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(eventData.getCaseDetails(),
                eventData.getLocalAuthorityCode());
        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyAdminOfPlacementApplicationUpload(PlacementApplicationEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> parameters = placementApplicationContentProvider
            .buildPlacementApplicationNotificationParameters(eventData.getCaseDetails());

        String email = getHmctsAdminEmail(eventData);

        notificationService.sendEmail(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void sendEmailsForIssuedCaseManagementOrder(final CaseManagementOrderIssuedEvent event) {
        EventData eventData = new EventData(event);

        sendCMOCaseLinkNotifications(eventData);
        sendCMODocumentLinkNotifications(eventData, event.getDocumentContents());
    }

    @EventListener
    public void sendEmailForCaseManagementOrderReadyForJudgeReview(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> parameters = caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(eventData.getCaseDetails());

        String email = getHmctsAdminEmail(eventData);

        notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void notifyLocalAuthorityOfRejectedCaseManagementOrder(final CaseManagementOrderRejectedEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> parameters =
            caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(
                eventData.getCaseDetails());

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(CMO_REJECTED_BY_JUDGE_TEMPLATE, recipientEmail, parameters,
            eventData.getReference());
    }

    @EventListener
    public void sendEmailForNoticeOfPlacementOrderUploaded(
        NoticeOfPlacementOrderUploadedEvent noticeOfPlacementEvent) {
        EventData eventData = new EventData(noticeOfPlacementEvent);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        Map<String, Object> parameters =
            localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(eventData.caseDetails);

        notificationService.sendEmail(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE, recipientEmail, parameters,
            eventData.reference);
        sendEmailToRepresentativesServedThroughDigitalService(eventData, parameters);
        sendOrderIssuedNotificationToAdmin(eventData, noticeOfPlacementEvent.getDocumentContents(),
            NOTICE_OF_PLACEMENT_ORDER);
    }


    //TODO: refactor to common method to send to parties. i.e sendEmailToRepresentative(NotificationId, FPLA-1249
    private void sendEmailToRepresentativesServedThroughDigitalService(EventData eventData,
                                                                              Map<String, Object> parameters) {
        CaseData caseData = objectMapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), DIGITAL_SERVICE);

        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> notificationService.sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                representative.getEmail(),
                parameters,
                eventData.getReference()));
    }

    private void sendCMOCaseLinkNotifications(final EventData eventData) {
        sendCMOCaseLinkNotificationForLocalAuthority(eventData);
        sendCMOCaseLinkNotificationToRepresentatives(eventData);
    }

    @EventListener
    public void sendEmailToPartiesAddedToCase(PartyAddedToCaseEvent event) {
        List<Representative> representatives = event.getRepresentativesToNotify();
        EventData eventData = new EventData(event);

        representatives.forEach(representative -> {
            String email = representative.getEmail();
            RepresentativeServingPreferences servingPreferences
                = representative.getServingPreferences();

            Map<String, Object> parameters = partyAddedToCaseContentProvider
                .getPartyAddedToCaseNotificationParameters(event.getCallbackRequest().getCaseDetails(),
                    servingPreferences);

            String template = partyAddedToCaseContentProvider
                .getPartyAddedToCaseNotificationTemplate(servingPreferences);

            notificationService.sendEmail(template, email, parameters,
                eventData.getReference());

        });
    }

    private void sendCMOCaseLinkNotificationForLocalAuthority(final EventData eventData) {
        final String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(
            eventData.getLocalAuthorityCode());

        Map<String, Object> localAuthorityNotificationParameters = caseManagementOrderEmailContentProvider
            .buildCMOIssuedCaseLinkNotificationParameters(eventData.getCaseDetails(), localAuthorityName);

        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE, email,
            localAuthorityNotificationParameters, eventData.getReference());
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

                notificationService.sendEmail(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
                    representative.getEmail(), representativeNotificationParameters, eventData.getReference());
            });
    }

    private void sendCMODocumentLinkNotifications(final EventData eventData, final byte[] documentContents) {
        sendCMODocumentLinkNotificationForCafcass(eventData, documentContents);
        sendCMODocumentLinkNotificationsToRepresentatives(eventData, documentContents);
        sendOrderIssuedNotificationToAdmin(eventData, documentContents, CMO);
    }

    private void sendCMODocumentLinkNotificationForCafcass(final EventData eventData, final byte[] documentContents) {
        final String cafcassName = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getName();

        Map<String, Object> cafcassParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
                eventData.getCaseDetails(), cafcassName, documentContents);

        String cafcassEmail = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        notificationService.sendEmail(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE, cafcassEmail,
            cafcassParameters, eventData.getReference());
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

                notificationService.sendEmail(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE,
                    representative.getEmail(), representativeNotificationParameters, eventData.getReference());
            });
    }

    private void sendOrderNotificationToLocalAuthority(final CaseDetails caseDetails,
                                                       final String localAuthorityCode,
                                                       final String mostRecentUploadedDocumentUrl) {
        Map<String, Object> localAuthorityParameters =
            orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                caseDetails, localAuthorityCode, mostRecentUploadedDocumentUrl);

        String recipientEmail = inboxLookupService.getNotificationRecipientEmail(caseDetails, localAuthorityCode);

        notificationService.sendEmail(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA, recipientEmail,
            localAuthorityParameters, Long.toString(caseDetails.getId()));
    }

    private void sendOrderIssuedNotificationToAdmin(final EventData eventData,
                                                    final byte[] documentContents,
                                                    final IssuedOrderType issuedOrderType) {
        Map<String, Object> parameters = orderIssuedEmailContentProvider.buildOrderNotificationParametersForHmctsAdmin(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, issuedOrderType);

        String email = getHmctsAdminEmail(eventData);

        notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN, email, parameters,
            Long.toString(eventData.getCaseDetails().getId()));
    }

    private String getHmctsAdminEmail(EventData eventData) {
        String ctscValue = getCtscValue(eventData.getCaseDetails().getData());

        if (ctscValue.equals("Yes")) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();
    }

    private String getCtscValue(Map<String, Object> caseData) {
        return caseData.get("sendToCtsc") != null ? caseData.get("sendToCtsc").toString() : "No";
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
