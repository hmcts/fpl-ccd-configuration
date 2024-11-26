package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CourtBundleUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_BUNDLE_UPLOADED_NOTIFICATION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceNotificationService {
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

    public Set<String> getDesignatedLocalAuthorityRecipientsOnly(CaseData caseData) {
        return new HashSet<>(localAuthorityRecipients.getDesignatedLocalAuthorityContacts(caseData));
    }

    public Set<String> getSecondaryLocalAuthorityRecipients(CaseData caseData) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .designatedLocalAuthorityExcluded(true)
            .build();

        return localAuthorityRecipients.getRecipients(recipientsRequest);
    }

    public Set<String> getSecondaryLocalAuthorityRecipientsOnly(CaseData caseData) {
        return new HashSet<>(localAuthorityRecipients.getSecondaryLocalAuthorityContacts(caseData));
    }

    public Set<String> getLegalRepresentativeOnly(CaseData caseData) {
        return new HashSet<>(localAuthorityRecipients.getLegalRepresentatives(caseData));
    }

    public Set<String> getFallbackInbox() {
        return new HashSet<>(localAuthorityRecipients.getFallbackInbox());
    }

    public Set<String> getRepresentativeEmails(CaseData caseData) {
        Set<String> emails = representativesInbox.getRepresentativeEmailsFilteredByRole(caseData,
            DIGITAL_SERVICE, List.of(CAFCASS, RESPONDENT));
        emails.addAll(representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE));
        emails.addAll(representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE));
        return emails;
    }

    public Set<String> getCafcassRepresentativeEmails(CaseData caseData) {
        return representativesInbox.getRepresentativeEmailsFilteredByRole(caseData,
            DIGITAL_SERVICE, List.of(CAFCASS));
    }

    public Set<String> getChildSolicitorEmails(CaseData caseData) {
        return new LinkedHashSet<>(representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE));
    }

    public Set<String> getChildSolicitorEmails(CaseData caseData, CaseRole caseRole) {
        return new LinkedHashSet<>(representativesInbox.getRepresentedSolicitorEmails(caseData, caseRole,
            DIGITAL_SERVICE));
    }

    public Set<String> getRespondentSolicitorEmails(CaseData caseData) {
        Set<String> emails = representativesInbox.getRepresentativeEmailsFilteredByRole(caseData,
            DIGITAL_SERVICE, List.of(RESPONDENT));
        emails.addAll(representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE));
        return emails;
    }

    public Set<String> getRespondentSolicitorEmails(CaseData caseData, CaseRole caseRole) {
        return new LinkedHashSet<>(representativesInbox.getRepresentedSolicitorEmails(caseData, caseRole,
            DIGITAL_SERVICE));
    }

    public void sendNotificationForCourtBundleUploaded(CaseData caseData, Set<String> recipients,
                                                       String hearingDetails) {
        if (!recipients.isEmpty()) {
            notificationService.sendEmail(COURT_BUNDLE_UPLOADED_NOTIFICATION,
                recipients,
                courtBundleUploadedEmailContentProvider.buildParameters(caseData, hearingDetails),
                caseData.getId().toString());
        }
    }

    public void sendNotification(CaseData caseData, Set<String> recipients,
                                 String sender, List<String> newNonConfidentialDocuments) {

        sendNotificationWithHearing(caseData, recipients, sender, newNonConfidentialDocuments, Optional.empty());
    }

    public void sendNotificationWithHearing(CaseData caseData, Set<String> recipients,
                                            String sender, List<String> newNonConfidentialDocuments,
                                            Optional<HearingBooking> hearingBooking) {

        String notificationTemplate = featureToggleService.isNewDocumentUploadNotificationEnabled()
            ? DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE : FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;

        if (!recipients.isEmpty()) {
            notificationService.sendEmail(notificationTemplate,
                recipients,
                furtherEvidenceUploadedEmailContentProvider.buildParametersWithHearing(caseData, sender,
                    newNonConfidentialDocuments, hearingBooking),
                caseData.getId().toString());
        }
    }
}
