package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private final FurtherEvidenceUploadDifferenceCalculator furtherEvidenceDifferenceCalculator;
    private final TranslationRequestService translationRequestService;
    private final SendDocumentService sendDocumentService;
    private static final String PDF = "pdf";

    @EventListener
    public void sendDocumentsUploadedNotification(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        DocumentUploadNotificationUserType userType = event.getUserType();
        List<Element<SupportingEvidenceBundle>> newBundle = getEvidenceBundle(caseData, userType);
        List<Element<SupportingEvidenceBundle>> oldBundle = getEvidenceBundle(caseDataBefore, userType);
        var newNonConfidentialDocuments = getNewNonConfidentialDocuments(newBundle, oldBundle);

        final Set<String> recipients = new HashSet<>();

        if (!newNonConfidentialDocuments.isEmpty()) {
            recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, userType));

            if (userType != LOCAL_AUTHORITY) {
                recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData));
            }
        }

        recipients.removeIf(email -> Objects.equals(email, uploader.getEmail()));

        if (isNotEmpty(recipients)) {
            List<String> newDocumentNames = getDocumentNames(newNonConfidentialDocuments);
            furtherEvidenceNotificationService.sendNotification(caseData, recipients, uploader.getFullName(),
                newDocumentNames);
        }
    }

    @EventListener
    public void sendDocumentsByPost(final FurtherEvidenceUploadedEvent event) {
        DocumentUploadNotificationUserType userType = event.getUserType();

        if (userType == SOLICITOR) {
            final CaseData caseData = event.getCaseData();
            final CaseData caseDataBefore = event.getCaseDataBefore();

            List<Element<SupportingEvidenceBundle>> newBundle = getEvidenceBundle(caseData, userType);
            List<Element<SupportingEvidenceBundle>> oldBundle = getEvidenceBundle(caseDataBefore, userType);
            var newNonConfidentialDocuments = getNewNonConfidentialDocuments(newBundle, oldBundle);

            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));
            List<DocumentReference> documents = getDocumentReferences(newNonConfidentialDocuments);
            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
        }
    }

    private List<SupportingEvidenceBundle> getNewNonConfidentialDocuments(
        List<Element<SupportingEvidenceBundle>> newBundle, List<Element<SupportingEvidenceBundle>> oldBundle) {

        List<SupportingEvidenceBundle> newDocs = new ArrayList<>();

        unwrapElements(newBundle).forEach(newDoc -> {
            if (!newDoc.isConfidentialDocument() && !unwrapElements(oldBundle).contains(newDoc)) {
                newDocs.add(newDoc);
            }
        });
        return newDocs;
    }

    private List<String> getDocumentNames(List<SupportingEvidenceBundle> documentBundle) {
        return documentBundle.stream().map(SupportingEvidenceBundle::getName).collect(Collectors.toList());
    }

    private List<DocumentReference> getDocumentReferences(List<SupportingEvidenceBundle> documentBundle) {
        List<DocumentReference> documentReferences = new ArrayList<>();

        documentBundle.forEach(doc -> {
            DocumentReference documentReference = doc.getDocument();
            if (hasExtension(documentReference.getFilename(), PDF)) {
                documentReferences.add(documentReference);
            }
        });

        return documentReferences;
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundle(CaseData caseData,
                                                                      DocumentUploadNotificationUserType userType) {
        if (userType == LOCAL_AUTHORITY) {
            return caseData.getFurtherEvidenceDocumentsLA();
        } else if (userType == SOLICITOR) {
            return caseData.getFurtherEvidenceDocumentsSolicitor();
        } else {
            return caseData.getFurtherEvidenceDocuments();
        }
    }


    @Async
    @EventListener
    public void notifyTranslationTeam(FurtherEvidenceUploadedEvent event) {
        furtherEvidenceDifferenceCalculator.calculate(event.getCaseData(), event.getCaseDataBefore())
            .forEach(bundle -> translationRequestService.sendRequest(event.getCaseData(),
                Optional.ofNullable(bundle.getValue().getTranslationRequirements()),
                bundle.getValue().getDocument(), bundle.getValue().asLabel())
            );
    }
}
