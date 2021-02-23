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
            if (hasNewNonConfidentialDocumentsByLA(caseData, caseDataBefore)) {
                notifyRespondents(caseData, excludedEmail, sender);
            }
        } else {
            if (hasNewNonConfidentialDocuments(caseData, caseDataBefore)) {
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

    private boolean hasNewNonConfidentialDocumentsByLA(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> furtherEvidenceDocumentsLA =
            unwrapElements(caseData.getFurtherEvidenceDocumentsLA());
        List<SupportingEvidenceBundle> oldFurtherEvidenceDocumentsLA =
            unwrapElements(caseDataBefore.getFurtherEvidenceDocumentsLA());

        return furtherEvidenceDocumentsLA.stream()
            .anyMatch(d -> oldFurtherEvidenceDocumentsLA.stream().noneMatch(old -> old.getName().equals(d.getName()))
                && !d.isConfidentialDocument());
    }

    private boolean hasNewNonConfidentialDocuments(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> furtherEvidenceDocuments =
            unwrapElements(caseData.getFurtherEvidenceDocuments());
        List<SupportingEvidenceBundle> oldFurtherEvidenceDocuments =
            unwrapElements(caseDataBefore.getFurtherEvidenceDocuments());

        return furtherEvidenceDocuments.stream()
            .anyMatch(d -> oldFurtherEvidenceDocuments.stream().noneMatch(old -> old.getName().equals(d.getName()))
                && !d.isConfidentialDocument());
    }

    private Set<String> filterRecipients(final Set<String> recipients, final String excludeEmail) {
        return StringUtils.isEmpty(excludeEmail) ? recipients :
            recipients.stream().filter(r -> !r.equals(excludeEmail)).collect(Collectors.toSet());
    }
}
