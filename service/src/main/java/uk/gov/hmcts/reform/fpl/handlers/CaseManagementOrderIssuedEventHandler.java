package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseManagementOrderIssuedEventHandler {
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;
    private final CaseManagementOrderEmailContentProvider contentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggleService toggleService;

    @EventListener
    public void notifyParties(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder issuedCmo = event.getCmo();

        String template = toggleService.isEldestChildLastNameEnabled()
                          ? CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE_CHILD_NAME
                          : CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;

        sendToLocalAuthority(caseData, issuedCmo, template);
        sendToCafcass(caseData, issuedCmo, template);
        sendToRepresentatives(caseData, issuedCmo, DIGITAL_SERVICE, template);
        sendToRepresentatives(caseData, issuedCmo, EMAIL, template);
        issuedOrderAdminNotificationHandler.notifyAdmin(caseData, issuedCmo.getOrder(), CMO);
    }

    private void sendToLocalAuthority(CaseData caseData, HearingOrder cmo, String template) {
        final IssuedCMOTemplate notifyData = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, cmo, DIGITAL_SERVICE
        );

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()
        );

        notificationService.sendEmail(template, emails, notifyData, caseData.getId().toString());
    }

    private void sendToCafcass(CaseData caseData, HearingOrder cmo, String template) {
        final IssuedCMOTemplate cafcassParameters = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, cmo, EMAIL
        );

        final String cafcassEmail = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(template, cafcassEmail, cafcassParameters, caseData.getId());
    }

    private void sendToRepresentatives(CaseData caseData, HearingOrder cmo,
                                       RepresentativeServingPreferences servingPreference, String template) {
        Set<String> representatives = representativesInbox.getEmailsByPreference(caseData, servingPreference);

        IssuedCMOTemplate notifyData = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, cmo, servingPreference
        );

        representatives.forEach(representative -> notificationService.sendEmail(
            template, representative, notifyData, caseData.getId()
        ));
    }

    @Async
    @EventListener
    public void sendDocumentToPostRepresentatives(final CaseManagementOrderIssuedEvent event) {
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            event.getCaseData().getId(),
            "internal-change-SEND_DOCUMENT",
            Map.of("documentToBeSent", event.getCmo().getOrder())
        );
    }
}
