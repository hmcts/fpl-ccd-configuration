package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEventHandler {
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final CourtService courtService;
    private final AdditionalApplicationsUploadedEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private static final String LIST = "•";

    @EventListener
    @Async
    public void sendAdditionalApplicationsByPost(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();
        final List<DocumentReference> documents = getApplicationDocuments(uploadedBundle);

        Set<Recipient> recipientsToNotify = getRecipientsToNotifyByPost(caseData, uploadedBundle);
        sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(recipientsToNotify));
    }

    @EventListener
    @Async
    public void sendDocumentsToCafcass(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
                cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent()) {
            AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();

            final CaseData caseDataBefore = event.getCaseDataBefore();
            AdditionalApplicationsBundle oldBundle =
                    Optional.ofNullable(caseDataBefore.getAdditionalApplicationsBundle())
                    .filter(Predicate.not(List::isEmpty))
                    .map(additionalApplicationsBundle -> additionalApplicationsBundle.get(0).getValue())
                    .orElse(null);

            if (!uploadedBundle.equals(oldBundle)) {
                String documentTypes = contentProvider.getApplicationTypes(uploadedBundle).stream()
                        .map(docType -> String.join(" ", LIST, docType))
                        .collect(Collectors.joining("\n"));

                final Set<DocumentReference> documentReferences = Set.copyOf(getApplicationDocuments(uploadedBundle));

                cafcassNotificationService.sendEmail(
                        caseData,
                        documentReferences,
                        ADDITIONAL_DOCUMENT,
                        NewDocumentData.builder()
                                .documentTypes(documentTypes)
                                .emailSubjectInfo("additional documents")
                                .build()
                );
            }
        }
    }

    private Set<Recipient> getRecipientsToNotifyByPost(CaseData caseData, AdditionalApplicationsBundle uploadedBundle) {
        Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

        List<Element<Other>> selectedOthers = getOthersSelected(uploadedBundle);
        List<Element<Respondent>> selectedRespondents = getRespondentsSelected(uploadedBundle);

        allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
            POST, caseData, selectedOthers, Element::getValue
        ));
        allRecipients.removeAll(representativesInbox.getNonSelectedRespondentRecipientsByPost(
            caseData, selectedRespondents
        ));

        allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers));
        allRecipients.addAll(representativesInbox.getSelectedRecipientsWithNoRepresentation(selectedRespondents));

        return allRecipients;
    }

    @EventListener
    @Async
    public void notifyAdmin(final AdditionalApplicationsUploadedEvent event) {
        List<String> roles = new ArrayList<>(requestData.userRoles());
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoleNames())) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = contentProvider.getNotifyData(caseData);
            String recipient = courtService.getCourtEmail(caseData);
            notificationService.sendEmail(
                INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, recipient, notifyData, caseData.getId());
        }
    }

    @EventListener
    @Async
    public void notifyApplicant(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final OrderApplicant applicant = event.getApplicant();

        final Set<String> recipients = new HashSet<>();

        if (applicant.getType() == LOCAL_AUTHORITY) {

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .build();

            recipients.addAll(localAuthorityRecipients.getRecipients(recipientsRequest));

        } else if (applicant.getType() == SECONDARY_LOCAL_AUTHORITY) {

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .build();

            recipients.addAll(localAuthorityRecipients.getRecipients(recipientsRequest));
        } else {

            final Map<String, String> respondentsEmails = getRespondentsEmails(caseData);

            if (isNotEmpty(respondentsEmails.get(applicant.getName()))) {
                recipients.add(respondentsEmails.get(applicant.getName()));
            }
        }

        if (isNotEmpty(recipients)) {
            sendNotification(caseData, recipients);
        }
    }

    private Map<String, String> getRespondentsEmails(CaseData caseData) {
        return caseData.getAllRespondents().stream()
            .collect(Collectors.toMap(
                respondent -> respondent.getValue().getParty().getFullName(),
                respondent -> hasNoSolicitorEmail(respondent) ? EMPTY
                    : respondent.getValue().getSolicitor().getEmail()
            ));
    }

    private boolean hasNoSolicitorEmail(Element<Respondent> respondent) {
        return isNull(respondent.getValue().getSolicitor()) || isEmpty(respondent.getValue().getSolicitor().getEmail());
    }

    private void sendNotification(CaseData caseData, Collection<String> emails) {
        NotifyData notifyData = contentProvider.getNotifyData(caseData);

        notificationService.sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, emails, notifyData,
            caseData.getId().toString()
        );
    }

    @EventListener
    @Async
    public void notifyDigitalRepresentatives(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        NotifyData notifyData = contentProvider.getNotifyData(caseData);

        Set<String> digitalRepresentativesEmails = getRepresentativesEmails(caseData, DIGITAL_SERVICE);

        representativeNotificationService.sendNotificationToRepresentatives(
            caseData.getId(),
            notifyData,
            digitalRepresentativesEmails,
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    private Set<String> getRepresentativesEmails(CaseData caseData,
                                                 RepresentativeServingPreferences servingPreference) {
        AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        List<Element<Other>> othersSelected = getOthersSelected(uploadedBundle);
        List<Element<Respondent>> respondentsSelected = getRespondentsSelected(uploadedBundle);

        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, servingPreference);

        Set<String> nonSelectedOthers = otherRecipientsInbox.getNonSelectedRecipients(
            servingPreference, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(nonSelectedOthers);

        Set<String> nonSelectedRespondentsRepresentatives = representativesInbox.getNonSelectedRespondentRecipients(
            servingPreference, caseData, respondentsSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(nonSelectedRespondentsRepresentatives);

        return digitalRepresentatives;
    }

    @EventListener
    @Async
    public void notifyEmailServedRepresentatives(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        NotifyData notifyData = contentProvider.getNotifyData(caseData);

        Set<String> emailRepresentatives = getRepresentativesEmails(caseData, EMAIL);

        if (!emailRepresentatives.isEmpty()) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
            );
        }
    }

    private List<DocumentReference> getApplicationDocuments(AdditionalApplicationsBundle bundle) {
        List<DocumentReference> documents = new ArrayList<>();

        if (bundle.getC2DocumentBundle() != null) {
            documents.add(bundle.getC2DocumentBundle().getDocument());
            documents.addAll(
                unwrapElements(bundle.getC2DocumentBundle().getSupplementsBundle())
                    .stream().map(Supplement::getDocument).collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(bundle.getC2DocumentBundle().getSupportingEvidenceBundle())
                    .stream().map(SupportingEvidenceBundle::getDocument).collect(Collectors.toList()));
        }

        if (bundle.getOtherApplicationsBundle() != null) {
            documents.add(bundle.getOtherApplicationsBundle().getDocument());
            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupplementsBundle())
                    .stream().map(Supplement::getDocument).collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupportingEvidenceBundle())
                    .stream().map(SupportingEvidenceBundle::getDocument).collect(Collectors.toList()));
        }

        return documents;
    }

    private List<Element<Other>> getOthersSelected(final AdditionalApplicationsBundle lastBundle) {
        if (lastBundle.getC2DocumentBundle() != null) {
            return defaultIfNull(lastBundle.getC2DocumentBundle().getOthers(), List.of());
        }

        return defaultIfNull(lastBundle.getOtherApplicationsBundle().getOthers(), List.of());
    }

    private List<Element<Respondent>> getRespondentsSelected(final AdditionalApplicationsBundle lastBundle) {
        if (lastBundle.getC2DocumentBundle() != null) {
            return defaultIfNull(lastBundle.getC2DocumentBundle().getRespondents(), List.of());
        }

        return defaultIfNull(lastBundle.getOtherApplicationsBundle().getRespondents(), List.of());
    }
}
