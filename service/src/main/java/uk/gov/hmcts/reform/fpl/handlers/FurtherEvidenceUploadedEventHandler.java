package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;

    @EventListener
    public void handleDocumentUploadedEvent(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final String excludedEmail = event.getInitiatedBy().getEmail();

        if (event.isUploadedByLA()) {
            if (hasNewNonConfidentialDocumentsByLA(caseData, caseDataBefore)) {
                notifyRespondents(caseData, excludedEmail);
            }
        } else {
            if (haseNewNonConfidentialDocuments(caseData, caseDataBefore)) {
                notifyLASolicitors(caseData, excludedEmail);
                notifyRespondents(caseData, excludedEmail);
            }
        }
    }

    private void notifyLASolicitors(final CaseData caseData, final String excludeEmail) {
        Set<String> recipients = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        if (!StringUtils.isEmpty(excludeEmail)) {
            recipients = recipients.stream().filter(r -> !r.equals(excludeEmail)).collect(Collectors.toSet());
        }

        if (!recipients.isEmpty()) {
            notificationService.sendEmail(
                FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
        }
    }

    private void notifyRespondents(final CaseData caseData, final String excludeEmail) {
        Set<String> recipients =  unwrapElements(caseData.getRespondents1()).stream()
            .map(Respondent::toParty)
            .map(Party::getEmail)
            .map(EmailAddress::getEmail)
            .collect(Collectors.toSet());

        if (!StringUtils.isEmpty(excludeEmail)) {
            recipients = recipients.stream().filter(r -> !r.equals(excludeEmail)).collect(Collectors.toSet());
        }

        if (!recipients.isEmpty()) {
            notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
        }
    }

    private boolean hasNewNonConfidentialDocumentsByLA(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> furtherEvidenceDocumentsLA = unwrapElements(caseData.getFurtherEvidenceDocumentsLA());
        List<SupportingEvidenceBundle> oldFurtherEvidenceDocumentsLA = unwrapElements(caseDataBefore.getFurtherEvidenceDocumentsLA());

        return furtherEvidenceDocumentsLA.stream()
            .anyMatch(d -> oldFurtherEvidenceDocumentsLA.stream().noneMatch(old -> old.getName().equals(d.getName())) &&
                !d.isConfidentialDocument() && !d.isUploadedByHMCTS());
    }

    private boolean haseNewNonConfidentialDocuments(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> furtherEvidenceDocuments = unwrapElements(caseData.getFurtherEvidenceDocuments());
        List<SupportingEvidenceBundle> oldFurtherEvidenceDocuments = unwrapElements(caseDataBefore.getFurtherEvidenceDocuments());

        return furtherEvidenceDocuments.stream()
            .anyMatch(d -> oldFurtherEvidenceDocuments.stream().noneMatch(old -> old.getName().equals(d.getName())) &&
                !d.isConfidentialDocument() && d.isUploadedByHMCTS());
    }
}
