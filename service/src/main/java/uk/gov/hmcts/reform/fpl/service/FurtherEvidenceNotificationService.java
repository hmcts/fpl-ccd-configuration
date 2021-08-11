package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceNotificationService {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;

    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    public Set<String> getLocalAuthoritiesRecipients(CaseData caseData) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        return localAuthorityRecipients.getRecipients(recipientsRequest);
    }

    public Set<String> getDesignatedLocalAuthorityRecipients(CaseData caseData) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        return localAuthorityRecipients.getRecipients(recipientsRequest);
    }

    public Set<String> getSecondaryLocalAuthorityRecipients(CaseData caseData) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .designatedLocalAuthorityExcluded(true)
            .build();

        return localAuthorityRecipients.getRecipients(recipientsRequest);
    }

    public Set<String> getRepresentativeEmails(CaseData caseData) {
        return unwrapElements(caseData.getRepresentatives()).stream()
            .filter(FurtherEvidenceNotificationService::notifyRepresentative)
            .map(Representative::getEmail)
            .collect(Collectors.toSet());
    }

    public void sendNotification(CaseData caseData, Set<String> recipients,
                                 String sender) {
        if (!recipients.isEmpty()) {
            notificationService.sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE,
                recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, sender),
                caseData.getId().toString());
        }
    }

    private static boolean notifyRepresentative(Representative rep) {
        return DIGITAL_SERVICE.equals(rep.getServingPreferences())
            && (CAFCASS.equals(rep.getRole().getType()) || RESPONDENT.equals(rep.getRole().getType()));
    }
}
