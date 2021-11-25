package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
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
    private final CafcassNotificationService cafcassNotificationService;
    private static final String PDF = "pdf";
    private static final String LIST = "•";

    @EventListener
    public void sendDocumentsUploadedNotification(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        DocumentUploaderType userType = event.getUserType();
        var newNonConfidentialDocuments = getDocuments(caseData,
            caseDataBefore,
            userType,
            (oldBundle, newDoc) -> !newDoc.isConfidentialDocument() && !unwrapElements(oldBundle).contains(newDoc));

        final Set<String> recipients = new HashSet<>();

        if (!newNonConfidentialDocuments.isEmpty()) {
            recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData, userType));

            if (userType == SECONDARY_LOCAL_AUTHORITY) {
                recipients.addAll(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            }

            if (userType == SOLICITOR || userType == HMCTS) {
                recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData));
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
        DocumentUploaderType userType = event.getUserType();

        if (userType == SOLICITOR) {
            final CaseData caseData = event.getCaseData();
            final CaseData caseDataBefore = event.getCaseDataBefore();

            var newNonConfidentialDocuments = getDocuments(caseData,
                caseDataBefore,
                userType,
                (oldBundle, newDoc) -> !newDoc.isConfidentialDocument() && !unwrapElements(oldBundle).contains(newDoc));

            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));
            List<DocumentReference> documents = getDocumentReferences(newNonConfidentialDocuments);
            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
        }
    }

    @EventListener
    public void sendCourtBundlesToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        List<CourtBundle> courtBundles = unwrapElements(caseData.getCourtBundleList());
        List<CourtBundle> oldCourtBundleList = unwrapElements(caseDataBefore.getCourtBundleList());

        Map<String, Set<DocumentReference>> newCourtBundles = courtBundles.stream()
                .filter(newDoc -> !oldCourtBundleList.contains(newDoc))
                .collect(groupingBy(CourtBundle::getHearing,
                        mapping(CourtBundle::getDocument, toSet())));

        newCourtBundles
            .forEach((key, value) ->
                cafcassNotificationService.sendEmail(
                        caseData,
                        value,
                        COURT_BUNDLE,
                        CourtBundleData.builder()
                                .hearingDetails(key)
                                .build()
                )
        );
    }

    @EventListener
    public void sendDocumentsToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final DocumentUploaderType userType = event.getUserType();

        var supportingEvidenceBundles = getDocuments(caseData,
            caseDataBefore,
            userType,
            (oldBundle, newDoc) -> !unwrapElements(oldBundle).contains(newDoc));

        var documentReferences = supportingEvidenceBundles.stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(Collectors.toSet());

        if (!documentReferences.isEmpty()) {
            var documentTypes = supportingEvidenceBundles.stream()
                    .map(document -> String.join(" ", LIST, document.getType().getLabel()))
                    .collect(Collectors.joining("\n"));

            cafcassNotificationService.sendEmail(
                    caseData,
                    documentReferences,
                    NEW_DOCUMENT,
                    NewDocumentData.builder().documentTypes(documentTypes).build()
            );
        }
    }

    private List<SupportingEvidenceBundle> getDocuments(
        CaseData caseData, CaseData caseDataBefore,
        DocumentUploaderType userType,
        BiPredicate<List<Element<SupportingEvidenceBundle>>, SupportingEvidenceBundle> biPredicate) {

        var newBundle = getEvidenceBundle(caseData, userType);
        var oldBundle = getEvidenceBundle(caseDataBefore, userType);

        List<SupportingEvidenceBundle> newDocs = new ArrayList<>();

        unwrapElements(newBundle).forEach(newDoc -> {
            if (biPredicate.test(oldBundle, newDoc)) {
                newDocs.add(newDoc);
            }
        });
        return newDocs;
    }


    private List<String> getDocumentNames(List<SupportingEvidenceBundle> documentBundle) {
        return documentBundle.stream().map(SupportingEvidenceBundle::getName).collect(toList());
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
                                                                      DocumentUploaderType uploaderType) {
        if (uploaderType == DESIGNATED_LOCAL_AUTHORITY || uploaderType == SECONDARY_LOCAL_AUTHORITY) {
            return caseData.getFurtherEvidenceDocumentsLA();
        }  else if (uploaderType == SOLICITOR) {
            List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle =
                defaultIfNull(caseData.getFurtherEvidenceDocumentsSolicitor(), List.of());
            List<Element<SupportingEvidenceBundle>> respondentStatementsBundle =
                getEvidenceBundleFromRespondentStatements(caseData);

            return concatEvidenceBundles(furtherEvidenceBundle, respondentStatementsBundle);
        } else {
            return caseData.getFurtherEvidenceDocuments();
        }
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundleFromRespondentStatements(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> evidenceBundle = new ArrayList<>();
        caseData.getRespondentStatements().forEach(statement ->
            evidenceBundle.addAll(statement.getValue().getSupportingEvidenceBundle())
        );
        return evidenceBundle;
    }

    private List<Element<SupportingEvidenceBundle>> concatEvidenceBundles(List<Element<SupportingEvidenceBundle>> b1,
                                                                          List<Element<SupportingEvidenceBundle>> b2) {
        return Stream.concat(b1.stream(), b2.stream()).collect(Collectors.toList());
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
