package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_2ND_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_CHILD_SOL;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_DESIGNATED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_RESP_SOL;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.childSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.respondentSolicitors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderRejectedEventHandler {
    private final NotificationService notificationService;
    private final CaseManagementOrderEmailContentProvider contentProvider;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private static final Predicate<HearingOrder> DESIGNATED_LA_SOLICITOR_FILTER =
        f -> f.getUploaderCaseRoles() == null || isEmpty(f.getUploaderCaseRoles())
            || CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.designatedLASolicitors());
    private static final Predicate<HearingOrder> SECONDARY_LA_SOLICITOR_FILTER =
        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), CaseRole.secondaryLASolicitors());

    @EventListener
    public void sendNotifications(final CaseManagementOrderRejectedEvent event) {
        final CaseData caseData = event.getCaseData();

        buildConfigurationMapGroupedByRecipient(event)
            .forEach((recipients, cmoRejected) -> {
                if (isNotEmpty(recipients) && isNotEmpty(cmoRejected)) {
                    RejectedCMOTemplate parameters = contentProvider.buildCMORejectedByJudgeNotificationParameters(
                        caseData, cmoRejected);
                    String templateId = getTemplateIdByRejectedHearingOrder(cmoRejected);
                    if (templateId != null) {
                        notificationService.sendEmail(templateId, recipients, parameters,
                            caseData.getId());
                    }
                }
            });
    }

    private String getTemplateIdByRejectedHearingOrder(HearingOrder cmoRejected) {
        if (DESIGNATED_LA_SOLICITOR_FILTER.test(cmoRejected)) {
            return CMO_REJECTED_BY_JUDGE_DESIGNATED_LA;
        } else if (SECONDARY_LA_SOLICITOR_FILTER.test(cmoRejected)) {
            return CMO_REJECTED_BY_JUDGE_2ND_LA;
        } else if (Stream.of(cmoRejected).anyMatch(f -> CollectionUtils
            .containsAny(f.getUploaderCaseRoles(), childSolicitors()))) {
            return CMO_REJECTED_BY_JUDGE_CHILD_SOL;
        } else if (Stream.of(cmoRejected).anyMatch(f -> CollectionUtils
            .containsAny(f.getUploaderCaseRoles(), respondentSolicitors()))) {
            return CMO_REJECTED_BY_JUDGE_RESP_SOL;
        } else {
            Stream.of(cmoRejected).forEach(ro ->
                log.info("Not sending notification for rejected orders: " + ro.getOrder().getFilename()
                    + "  uploaded by " + ro.getUploaderCaseRoles()));
            return null;
        }
    }

    private Map<Set<String>, HearingOrder> buildConfigurationMapGroupedByRecipient(
        final CaseManagementOrderRejectedEvent event) {
        final CaseData caseData = event.getCaseData();

        Map<Set<String>, HearingOrder> resultMap = new HashMap<>();

        // designated LA
        Set<String> designatedLA = furtherEvidenceNotificationService
            .getDesignatedLocalAuthorityRecipientsOnly(caseData);
        if (designatedLA.isEmpty()) {
            log.info("No recipient found for designated LA");
        } else {
            resultMap.put(designatedLA,
                Stream.of(event.getCmo()).filter(DESIGNATED_LA_SOLICITOR_FILTER).findAny().orElse(null));
        }

        // secondary LA
        Set<String> secondaryLA = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData);
        if (secondaryLA.isEmpty()) {
            log.info("No recipient found for secondary LA");
        } else {
            resultMap.put(secondaryLA,
                Stream.of(event.getCmo()).filter(SECONDARY_LA_SOLICITOR_FILTER).findAny().orElse(null));
        }

        // respondent solicitors
        for (CaseRole caseRole : respondentSolicitors()) {
            Set<String> childSolicitor = furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData,
                caseRole);
            if (childSolicitor.isEmpty()) {
                log.info("No recipient found for " + caseRole);
            } else {
                resultMap.put(childSolicitor,
                    Stream.of(event.getCmo()).filter(
                        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), List.of(caseRole))
                    ).findAny().orElse(null));
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
                    Stream.of(event.getCmo()).filter(
                        f -> CollectionUtils.containsAny(f.getUploaderCaseRoles(), List.of(caseRole))
                    ).findAny().orElse(null));
            }
        }

        return resultMap;
    }
}
