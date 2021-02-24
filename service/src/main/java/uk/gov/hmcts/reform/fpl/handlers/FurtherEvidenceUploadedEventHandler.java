package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @EventListener
    public void handleDocumentUploadedEvent(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final String excludedEmail = event.getInitiatedBy().getEmail();
        final String sender = event.getInitiatedBy().getFullName();

        if (event.isUploadedByLA()) {
            if (hasNewNonConfidentialDocuments(caseData.getFurtherEvidenceDocumentsLA(),
                caseDataBefore.getFurtherEvidenceDocumentsLA())) {
                notifyRespondents(caseData, excludedEmail, sender);
            }
        } else {
            if (hasNewNonConfidentialDocuments(caseData.getFurtherEvidenceDocuments(),
                caseDataBefore.getFurtherEvidenceDocuments())) {
                notifyLASolicitors(caseData, excludedEmail, sender);
                notifyRespondents(caseData, excludedEmail, sender);
            }
        }
    }

    private void notifyLASolicitors(final CaseData caseData, final String excludeEmail, final String sender) {
        Set<String> recipients = furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData);
        furtherEvidenceNotificationService.sendFurtherEvidenceDocumentsUploadedNotification(
            caseData, filterRecipients(recipients, excludeEmail), sender);
    }

    private void notifyRespondents(final CaseData caseData, final String excludeEmail, final String sender) {
        Set<String> recipients = furtherEvidenceNotificationService.getRespondentRepresentativeEmails(caseData);
        furtherEvidenceNotificationService.sendFurtherEvidenceDocumentsUploadedNotification(
            caseData, filterRecipients(recipients, excludeEmail), sender);
    }

    private static boolean hasNewNonConfidentialDocuments(List<Element<SupportingEvidenceBundle>> newEvidenceBundle,
                                                          List<Element<SupportingEvidenceBundle>> oldEvidenceBundle) {
        List<SupportingEvidenceBundle> oldEvidenceBundleUnwrapped = unwrapElements(oldEvidenceBundle);
        return unwrapElements(newEvidenceBundle).stream()
            .anyMatch(d -> oldEvidenceBundleUnwrapped.stream()
                .noneMatch(old -> old.getDocument().equals(d.getDocument()))
                && !d.isConfidentialDocument());
    }

    private static Set<String> filterRecipients(final Set<String> recipients, final String excludeEmail) {
        return StringUtils.isEmpty(excludeEmail) ? recipients :
            recipients.stream().filter(r -> !r.equals(excludeEmail)).collect(Collectors.toSet());
    }
}
