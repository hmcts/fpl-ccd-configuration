package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.draftCMO.ApprovedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE_NEW;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderCaseLinkNotificationHandler {
    private final ObjectMapper mapper;
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final RepresentativeService representativeService;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    public void sendNotifications(final EventData eventData, final byte[] documentContents) {
        sendToLocalAuthority(eventData, documentContents);
        sendToRepresentatives(eventData, documentContents);
    }

    private void sendToLocalAuthority(final EventData eventData, final byte[] documentContents) {
        final ApprovedCMOTemplate localAuthorityNotificationParameters = caseManagementOrderEmailContentProvider
            .buildCMOIssuedNotificationParameters(eventData.getCaseDetails(), DIGITAL_SERVICE, documentContents);

        final String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE_NEW, email,
            localAuthorityNotificationParameters, eventData.getReference());
    }

    private void sendToRepresentatives(final EventData eventData, final byte[] documentContents) {
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        List<Representative> representatives = representativeService.getRepresentativesByServedPreference(
            caseData.getRepresentatives(), DIGITAL_SERVICE);

        representatives.stream()
            .filter(representative -> isNotBlank(representative.getEmail()))
            .forEach(representative -> {
                ApprovedCMOTemplate representativeNotificationParameters =
                    caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
                        eventData.getCaseDetails(), DIGITAL_SERVICE, documentContents);

                notificationService.sendEmail(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE_NEW,
                    representative.getEmail(), representativeNotificationParameters, eventData.getReference());
            });
    }
}
