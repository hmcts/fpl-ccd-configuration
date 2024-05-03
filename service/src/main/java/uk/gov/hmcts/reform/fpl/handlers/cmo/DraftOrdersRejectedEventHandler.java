package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS_2ND_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS_CHILD_SOL;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS_DESIGNATED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_REJECTS_DRAFT_ORDERS_RESP_SOL;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.childSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.respondentSolicitors;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersRejectedEventHandler {

    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private static final Predicate<HearingOrder> DESIGNATED_LA_SOLICITOR_FILTER =
        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.designatedLASolicitors());
    private static final Predicate<HearingOrder> SECONDARY_LA_SOLICITOR_FILTER =
        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.secondaryLASolicitors());

    @Async
    @EventListener
    public void sendNotifications(final DraftOrdersRejected event) {
        final CaseData caseData = event.getCaseData();
        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        buildConfigurationMapGroupedByRecipient(event)
            .forEach((recipients, theirDraftOrderRejected) -> {
                if (isNotEmpty(recipients)) {
                    final RejectedOrdersTemplate content = contentProvider.buildOrdersRejectedContent(
                        caseData, hearing, theirDraftOrderRejected);
                    String templateId = getTemplateIdByRejectedHearingOrder(theirDraftOrderRejected);
                    if (templateId != null) {
                        notificationService.sendEmail(templateId, recipients, content,
                            caseData.getId());
                    }
                }
            });
    }

    private String getTemplateIdByRejectedHearingOrder(List<HearingOrder> rejectedOrders) {
        if (rejectedOrders.stream().anyMatch(DESIGNATED_LA_SOLICITOR_FILTER)) {
            return JUDGE_REJECTS_DRAFT_ORDERS_DESIGNATED_LA;
        } else if (rejectedOrders.stream().anyMatch(SECONDARY_LA_SOLICITOR_FILTER)) {
            return JUDGE_REJECTS_DRAFT_ORDERS_2ND_LA;
        } else if (rejectedOrders.stream().anyMatch(f -> CollectionUtils
            .containsAny(f.getUploaderCaseRoles(), childSolicitors()))) {
            return JUDGE_REJECTS_DRAFT_ORDERS_CHILD_SOL;
        } else if (rejectedOrders.stream().anyMatch(f -> CollectionUtils
            .containsAny(f.getUploaderCaseRoles(), respondentSolicitors()))) {
            return JUDGE_REJECTS_DRAFT_ORDERS_RESP_SOL;
        } else {
            rejectedOrders.forEach(ro ->
                log.info("Not sending notification for rejected orders: " + ro.getOrder().getFilename()
                    + "  uploaded by " + ro.getUploaderCaseRoles()));
            return null;
        }
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
                event.getRejectedOrders().stream().filter(DESIGNATED_LA_SOLICITOR_FILTER).toList());
        }

        // secondary LA
        Set<String> secondaryLA = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData);
        if (secondaryLA.isEmpty()) {
            log.info("No recipient found for secondary LA");
        } else {
            resultMap.put(secondaryLA,
                event.getRejectedOrders().stream().filter(SECONDARY_LA_SOLICITOR_FILTER).toList());
        }

        // respondent solicitors
        for (CaseRole caseRole : respondentSolicitors()) {
            Set<String> childSolicitor = furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData,
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

        // child solicitors
        for (CaseRole caseRole : childSolicitors()) {
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
