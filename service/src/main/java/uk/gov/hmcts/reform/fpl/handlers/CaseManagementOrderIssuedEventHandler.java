package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.draftcmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderIssuedEventHandler {
    private final ObjectMapper mapper;
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;

    @EventListener
    public void sendEmailsForIssuedCaseManagementOrder(final CaseManagementOrderIssuedEvent event) {
        EventData eventData = new EventData(event);

        sendToLocalAuthority(eventData);
        sendToCafcass(eventData);
        sendToRepresentatives(eventData);

        // TODO
        // Refactor issuedOrderAdminNotificationHandler to use Java object for template so we can avoid passing byte
        // to method
        issuedOrderAdminNotificationHandler.sendToAdmin(eventData, event.getDocumentContents(), CMO);
    }

    private void sendToLocalAuthority(final EventData eventData) {
        final IssuedCMOTemplate localAuthorityNotificationParameters = caseManagementOrderEmailContentProvider
            .buildCMOIssuedNotificationParameters(eventData.getCaseDetails(), DIGITAL_SERVICE);

        final String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, email,
            localAuthorityNotificationParameters, eventData.getReference());
    }

    private void sendToCafcass(final EventData eventData) {
        final IssuedCMOTemplate cafcassParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
                eventData.getCaseDetails(), EMAIL);

        final String cafcassEmail = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        notificationService.sendEmail(CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, cafcassEmail, cafcassParameters,
            eventData.getReference());
    }

    private void sendToRepresentatives(final EventData eventData) {
        IssuedCMOTemplate digitalServiceRepresentativeNotificationParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
                eventData.getCaseDetails(), DIGITAL_SERVICE);

        IssuedCMOTemplate emailRepresentativeNotificationParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
                eventData.getCaseDetails(), EMAIL);

        representativeNotificationService.sendToRepresentativesByServedPreference(DIGITAL_SERVICE,
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, digitalServiceRepresentativeNotificationParameters.toMap(mapper),
            eventData);

        representativeNotificationService.sendToRepresentativesByServedPreference(EMAIL,
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, emailRepresentativeNotificationParameters.toMap(mapper), eventData);
    }
}
