package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.DraftOrderUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersRejectedEventHandler {

    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;

//    @Async
//    @EventListener
//    public void sendNotificationToLA(final DraftOrdersRejected event) {
//        CaseData caseData = event.getCaseData();
//        List<HearingOrder> rejectedOrders = event.getRejectedOrders();
//
//        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
//            caseData.getHearingDetails())
//            .map(Element::getValue)
//            .orElse(null);
//
//        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
//            .caseData(caseData)
//            .secondaryLocalAuthorityExcluded(true)
//            .build();
//
//        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);
//
//        final RejectedOrdersTemplate content = contentProvider.buildOrdersRejectedContent(
//            caseData, hearing, rejectedOrders
//        );
//
//        notificationService.sendEmail(JUDGE_REJECTS_DRAFT_ORDERS, recipients, content, caseData.getId());
//    }

    @Async
    @EventListener
    public void sendNotifications(final DraftOrdersRejected event) {
        buildConfigurationMapGroupedByRecipient(event)
            .forEach((recipients, theirDraftOrderRejected) -> {
                if (isNotEmpty(recipients)) {
                    // TODO replacing logging to notificationService.
                    recipients.forEach(r -> theirDraftOrderRejected.stream().forEach(d ->
                        log.info(d.getOrder().getFilename() + " rejected. Notifying " + r)));
                }
            });
    }

    private Map<Set<String>, List<HearingOrder>> buildConfigurationMapGroupedByRecipient(
        final DraftOrdersRejected event) {
        final CaseData caseData = event.getCaseData();

        Map<Set<String>, List<HearingOrder>> resultMap = new HashMap<>();

        // designated LA
        Set<String> designatedLA = furtherEvidenceNotificationService
            .getDesignatedLocalAuthorityRecipientsOnly(caseData);
        if (designatedLA.isEmpty()) {
            log.info("No recipient found for designated LA");
        } else {
            resultMap.put(designatedLA,
                event.getRejectedOrders().stream().filter(
                    f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.designatedLASolicitors())
                ).toList());
        }

        // secondary LA
        Set<String> secondaryLA = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData);
        if (secondaryLA.isEmpty()) {
            log.info("No recipient found for secondary LA");
        } else {
            resultMap.put(secondaryLA,
                event.getRejectedOrders().stream().filter(
                    f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.secondaryLASolicitors())
                ).toList());
        }

        // TODO respondent solicitor
//        Set<String> respondentSolicitor = furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData);
//        if (respondentSolicitor.isEmpty()) {
//            log.info("No recipient found for respondent solicitor");
//        } else {
//            resultMap.put(respondentSolicitor,
//                DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor);
//        }
//
        for (CaseRole caseRole : CaseRole.childSolicitors()) {
            Set<String> childSolicitor = furtherEvidenceNotificationService.getChildSolicitorEmails(caseData,
                caseRole);
            if (childSolicitor.isEmpty()) {
                log.info("No recipient found for " + caseRole);
            } else {
                resultMap.put(childSolicitor,
                    event.getRejectedOrders().stream().filter(
                        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), List.of(caseRole))
                    ).toList());
            }
        }

        return resultMap;
    }
}
