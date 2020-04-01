package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderDocumentLinkNotificationHandler {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final RepresentativeService representativeService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    public void sendNotifications(final EventData eventData, final byte[] documentContents) {
        sendToCafcass(eventData, documentContents);
        sendToRepresentatives(eventData, documentContents);
        issuedOrderAdminNotificationHandler.sendOrderIssuedNotificationToAdmin(eventData, documentContents, CMO);
    }

    private void sendToCafcass(final EventData eventData, final byte[] documentContents) {
        final String cafcassName = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getName();

        final Map<String, Object> cafcassParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
                eventData.getCaseDetails(), cafcassName, documentContents);

        final String cafcassEmail = cafcassLookupConfiguration.getCafcass(eventData.getLocalAuthorityCode()).getEmail();

        notificationService.sendEmail(CMO_ORDER_ISSUED_DOCUMENT_LINK_NOTIFICATION_TEMPLATE, cafcassEmail,
            cafcassParameters, eventData.getReference());
    }

    private void sendToRepresentatives(final EventData eventData,
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
}
