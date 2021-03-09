package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceNotificationService {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    public Set<String> getLocalAuthoritySolicitorEmails(CaseData caseData) {
        return inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
    }

    public Set<String> getRespondentRepresentativeEmails(CaseData caseData) {
        return unwrapElements(caseData.getRepresentatives()).stream()
            .filter(rep -> RESPONDENT.equals(rep.getRole().getType()))
            .filter(rep -> DIGITAL_SERVICE.equals(rep.getServingPreferences()))
            .map(Representative::getEmail)
            .collect(Collectors.toSet());
    }

    public void sendFurtherEvidenceDocumentsUploadedNotification(CaseData caseData, Set<String> recipients,
                                                                 String sender) {
        if (!recipients.isEmpty()) {
            notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE,
                recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, sender),
                caseData.getId().toString());
        }
    }
}
