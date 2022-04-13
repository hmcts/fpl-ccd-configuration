package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CourtBundleUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_BUNDLE_UPLOADED_NOTIFICATION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentUploadedNotificationService {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;
    private final FeatureToggleService featureToggleService;

    private final FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    private final CourtBundleUploadedEmailContentProvider courtBundleUploadedEmailContentProvider;

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
        List<RepresentativeRole.Type> roles = List.of(CAFCASS, RESPONDENT);
        HashSet<String> emails = representativesInbox.getRepresentativeEmailsFilteredByRole(caseData,
            DIGITAL_SERVICE, roles);
        emails.addAll(representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE));
        emails.addAll(representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE));
        return emails;
    }

    public void sendNotificationForCourtBundleUploaded(CaseData caseData, Set<String> recipients,
                                                       String hearingDetails) {
        String notificationTemplate = COURT_BUNDLE_UPLOADED_NOTIFICATION;
        if (!recipients.isEmpty()) {
            notificationService.sendEmail(notificationTemplate,
                recipients,
                courtBundleUploadedEmailContentProvider.buildParameters(caseData, hearingDetails),
                caseData.getId().toString());
        }
    }

    public void sendNotification(CaseData caseData, Set<String> recipients,
                                 String sender, List<String> newNonConfidentialDocuments) {

        String notificationTemplate = featureToggleService.isNewDocumentUploadNotificationEnabled()
            ? DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE : FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;

        if (!recipients.isEmpty()) {
            notificationService.sendEmail(notificationTemplate,
                recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, sender,
                    newNonConfidentialDocuments),
                caseData.getId().toString());
        }
    }
}
