package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final RepresentativeService representativeService;

    @EventListener
    public void handleDocumentUploadedEvent(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final String excludedEmail = event.getInitiatedBy().getEmail();

        switch(event.getUploadedBy()) {
            case "LA_SOLICITOR":
                notifySolicitors(caseData, caseDataBefore, excludedEmail);
                break;
            case "SOLICITOR":
            case "HMCTS_USER":
                notifyLASolicitors(caseData, caseDataBefore, excludedEmail);
                notifySolicitors(caseData, caseDataBefore, excludedEmail);
                break;
            default:
                log.error("Further evidence uploaded by unknown user type {}", event.getUploadedBy());
                break;
        }
    }

    private void notifyLASolicitors(final CaseData caseData, final CaseData caseDataBefore, final String excludeEmail) {
        Set<String> recipients = inboxLookupService.getRecipients(LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
        if(!StringUtils.isEmpty(excludeEmail))
        {
           recipients = recipients.stream().filter( r -> r.equals(excludeEmail)).collect(Collectors.toSet());
        }
        notificationService.sendEmail(
            FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
    }

    private void notifySolicitors(final CaseData caseData, final CaseData caseDataBefore, final String  excludeEmail) {
        if(hasNewNonConfidentialDocumentsByLA(caseData, caseDataBefore)) {
            Set<String> recipients = caseData.getRepresentativesByServedPreference(EMAIL).stream()
                .map(Representative::getEmail)
                .collect(Collectors.toSet());

            recipients.add(caseData.getSolicitor().getEmail());

            if(!StringUtils.isEmpty(excludeEmail))
            {
                recipients = recipients.stream().filter(r -> !r.equals(excludeEmail)).collect(Collectors.toSet());
            }

            if(!recipients.isEmpty()) {
                notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
                    furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData), caseData.getId().toString());
            }
        }
    }

    private boolean hasNewNonConfidentialDocumentsByLA(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> furtherEvidenceDocumentsLA = unwrapElements( caseData.getFurtherEvidenceDocumentsLA());
        List<SupportingEvidenceBundle> oldFurtherEvidenceDocumentsLA = unwrapElements(caseDataBefore.getFurtherEvidenceDocumentsLA());

        return furtherEvidenceDocumentsLA.stream().anyMatch(d -> oldFurtherEvidenceDocumentsLA.stream().noneMatch(old -> old.getName().equals(d.getName())) &&
            !d.isConfidentialDocument() && !d.isUploadedByHMCTS());
    }
}
