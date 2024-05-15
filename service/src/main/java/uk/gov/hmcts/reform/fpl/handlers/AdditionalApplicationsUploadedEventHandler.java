package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
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
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@Slf4j
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
        AdditionalApplicationsBundle uploadedBundle = getUploadedBundle(caseData);
        if (!isConfidentialC2UploadedOnly(uploadedBundle)) {
            final List<DocumentReference> documents = getApplicationDocuments(uploadedBundle, false);

            Set<Recipient> recipientsToNotify = getRecipientsToNotifyByPost(caseData);
            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(recipientsToNotify));
        }
    }

    @EventListener
    @Async
    public void sendDocumentsToCafcass(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            AdditionalApplicationsBundle uploadedBundle = getUploadedBundle(caseData);

            if (!isConfidentialC2UploadedOnly(uploadedBundle)
                    || isConfidentialC2UploadedByChildSolicitor(uploadedBundle)) {
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

                    final Set<DocumentReference> documentReferences =
                        Set.copyOf(getApplicationDocuments(uploadedBundle, true));

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
    }

    private Set<Recipient> getRecipientsToNotifyByPost(CaseData caseData) {
        return new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));
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

        AdditionalApplicationsBundle newBundleUploaded = getUploadedBundle(caseData);

        // DFPL-498 notify all LAs
        final Set<String> recipients = new HashSet<>();

        if (!isConfidentialC2UploadedOnly(newBundleUploaded)) {
            recipients.addAll(localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
                .caseData(caseData).build()));

            if (!List.of(LOCAL_AUTHORITY, SECONDARY_LOCAL_AUTHORITY).contains(applicant.getType())) {
                final Map<String, String> emails = getRespondentAndChildEmails(caseData);
                if (isNotEmpty(emails.get(applicant.getName()))) {
                    recipients.add(emails.get(applicant.getName()));
                }
            }
        }

        if (isNotEmpty(recipients)) {
            sendNotification(caseData, recipients);
        }
    }

    private Map<String, String> getRespondentAndChildEmails(CaseData caseData) {
        Map<String, String> emails = getRespondentsEmails(caseData);
        emails.putAll(getChildrenEmails(caseData));
        return emails;
    }

    private Map<String, String> getRespondentsEmails(CaseData caseData) {
        return caseData.getAllRespondents().stream()
            .collect(Collectors.toMap(
                respondent -> respondent.getValue().getParty().getFullName(),
                respondent -> hasNoSolicitorEmail(respondent) ? EMPTY
                    : respondent.getValue().getSolicitor().getEmail()
            ));
    }

    private Map<String, String> getChildrenEmails(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .collect(Collectors.toMap(
                child -> child.getValue().getParty().getFullName(),
                child -> hasNoSolicitorEmail(child) ? EMPTY
                    : child.getValue().getSolicitor().getEmail()
            ));
    }

    private boolean hasNoSolicitorEmail(Element<? extends WithSolicitor> respondent) {
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
        AdditionalApplicationsBundle bundleUploaded = getUploadedBundle(caseData);

        if (!isConfidentialC2UploadedOnly(bundleUploaded)) {
            NotifyData notifyData = contentProvider.getNotifyData(caseData);

            Set<String> digitalRepresentativesEmails = getRepresentativesEmails(caseData, DIGITAL_SERVICE);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                digitalRepresentativesEmails,
                INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
            );
        }
    }

    private Set<String> getRepresentativesEmails(CaseData caseData,
                                                 RepresentativeServingPreferences servingPreference) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, servingPreference);

        // Using this to ensure all others are removed, this will be deprecated once other flows
        // are updated to not notify others.
        Set<String> nonSelectedOthers = otherRecipientsInbox.getNonSelectedRecipients(
            servingPreference, caseData, new ArrayList<>(), element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(nonSelectedOthers);

        return digitalRepresentatives;
    }

    @EventListener
    @Async
    public void notifyEmailServedRepresentatives(final AdditionalApplicationsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        AdditionalApplicationsBundle bundleUploaded = getUploadedBundle(caseData);

        if (!isConfidentialC2UploadedOnly(bundleUploaded)) {
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
    }

    private List<DocumentReference> getApplicationDocuments(
        AdditionalApplicationsBundle bundle, boolean includeConfidential) {
        UnaryOperator<DocumentReference> addDocumentType =
            documentReference -> {
                documentReference.setType(ADDITIONAL_DOCUMENT.getLabel());
                return documentReference;
            };

        List<DocumentReference> documents = new ArrayList<>();

        documents.addAll(getC2DocumentList(addDocumentType, bundle.getC2DocumentBundle()));
        if (includeConfidential) {
            documents.addAll(getC2DocumentList(addDocumentType, bundle.getC2DocumentBundleConfidential()));
        }

        if (bundle.getOtherApplicationsBundle() != null) {
            documents.add(Optional.ofNullable(bundle.getOtherApplicationsBundle().getDocument())
                .map(addDocumentType)
                .orElse(DocumentReference.builder().build()));
            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupplementsBundle())
                    .stream()
                    .map(Supplement::getDocument)
                    .map(addDocumentType)
                    .collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupportingEvidenceBundle())
                    .stream()
                    .map(SupportingEvidenceBundle::getDocument)
                    .map(addDocumentType)
                    .collect(Collectors.toList()));
        }

        return documents;
    }

    private List<DocumentReference> getC2DocumentList(UnaryOperator<DocumentReference> addDocumentType,
                                                      C2DocumentBundle c2Bundle) {

        List<DocumentReference> documents = new ArrayList<>();
        if (c2Bundle != null) {
            documents.add(Optional.ofNullable(c2Bundle.getDocument())
                .map(addDocumentType)
                .orElse(DocumentReference.builder().build()));
            documents.addAll(
                unwrapElements(c2Bundle.getSupplementsBundle())
                    .stream()
                    .map(Supplement::getDocument)
                    .map(addDocumentType)
                    .collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(c2Bundle.getSupportingEvidenceBundle())
                    .stream()
                    .map(SupportingEvidenceBundle::getDocument)
                    .map(addDocumentType)
                    .collect(Collectors.toList()));
        }

        return documents;
    }

    private AdditionalApplicationsBundle getUploadedBundle(CaseData caseData) {
        return caseData.getAdditionalApplicationsBundle().get(0).getValue();
    }

    private boolean isConfidentialC2UploadedOnly(AdditionalApplicationsBundle bundle) {
        return bundle.getC2DocumentBundle() == null && bundle.getOtherApplicationsBundle() == null;
    }

    private boolean isConfidentialC2UploadedByChildSolicitor(AdditionalApplicationsBundle bundle) {
        return isNotEmpty(bundle.getC2DocumentBundleChild0()) || isNotEmpty(bundle.getC2DocumentBundleChild1())
               || isNotEmpty(bundle.getC2DocumentBundleChild2()) || isNotEmpty(bundle.getC2DocumentBundleChild3())
               || isNotEmpty(bundle.getC2DocumentBundleChild4()) || isNotEmpty(bundle.getC2DocumentBundleChild5())
               || isNotEmpty(bundle.getC2DocumentBundleChild6()) || isNotEmpty(bundle.getC2DocumentBundleChild7())
               || isNotEmpty(bundle.getC2DocumentBundleChild8()) || isNotEmpty(bundle.getC2DocumentBundleChild9())
               || isNotEmpty(bundle.getC2DocumentBundleChild10()) || isNotEmpty(bundle.getC2DocumentBundleChild11())
               || isNotEmpty(bundle.getC2DocumentBundleChild12()) || isNotEmpty(bundle.getC2DocumentBundleChild13())
               || isNotEmpty(bundle.getC2DocumentBundleChild14());
    }
}
