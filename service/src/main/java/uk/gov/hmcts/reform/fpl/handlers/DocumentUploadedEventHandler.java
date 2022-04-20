package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.DocumentUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.DocumentInfo;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;
import uk.gov.hmcts.reform.fpl.service.DocumentUploadedNotificationService;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentUploadedEventHandler {
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";
    private final DocumentUploadedNotificationService documentUploadedNotificationService;
    private final FurtherEvidenceUploadDifferenceCalculator furtherEvidenceDifferenceCalculator;
    private final TranslationRequestService translationRequestService;
    private final SendDocumentService sendDocumentService;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private static final String PDF = "pdf";
    private static final String LIST = "•";

    @EventListener
    public void sendDocumentsUploadedNotification(final DocumentUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        DocumentUploaderType userType = event.getUserType();
        var newNonConfidentialDocuments = getNewUploadedFurtherDocuments(caseData,
            caseDataBefore,
            (oldBundle, newDoc) -> !newDoc.isConfidentialDocument() && !oldBundle.contains(newDoc));

        final Set<String> recipients = new HashSet<>();

        if (!newNonConfidentialDocuments.isEmpty()) {
            recipients.addAll(documentUploadedNotificationService.getRepresentativeEmails(caseData));
            recipients.addAll(documentUploadedNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            recipients.addAll(documentUploadedNotificationService.getLocalAuthoritiesRecipients(caseData));
        }

        if (isNotEmpty(recipients)) {
            List<String> newDocumentNames = getDocumentNames(newNonConfidentialDocuments);

            documentUploadedNotificationService.sendNotification(caseData, recipients, uploader.getFullName(),
                newDocumentNames);
        }
    }

    @EventListener
    public void sendDocumentsByPost(final DocumentUploadedEvent event) {
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
    public void sendCourtBundlesUploadedNotification(final DocumentUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        final CaseData caseDataBefore = event.getCaseDataBefore();
        List<CourtBundle> courtBundles = unwrapElements(caseData.getCourtBundleList());
        List<CourtBundle> oldCourtBundleList = unwrapElements(caseDataBefore.getCourtBundleList());

        Map<String, Set<DocumentReference>> newCourtBundles = courtBundles.stream()
            .filter(newDoc -> !oldCourtBundleList.contains(newDoc))
            .collect(groupingBy(CourtBundle::getHearing,
                mapping(CourtBundle::getDocument, toSet())));

        final Set<String> recipients = new HashSet<>();

        if (!newCourtBundles.isEmpty()) {
            recipients.addAll(documentUploadedNotificationService.getRepresentativeEmails(caseData));
            recipients.addAll(documentUploadedNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            recipients.addAll(documentUploadedNotificationService.getLocalAuthoritiesRecipients(caseData));
        }

        if (isNotEmpty(recipients)) {
            newCourtBundles
                .forEach((hearingDetails, value) ->
                    documentUploadedNotificationService.sendNotificationForCourtBundleUploaded(caseData, recipients,
                        hearingDetails));
        }
    }

    @EventListener
    public void sendCourtBundlesToCafcass(final DocumentUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
                cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent()) {
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
                            ));
        }
    }

    @EventListener
    public void sendDocumentsToCafcass(final DocumentUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
                cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent()) {
            final CaseData caseDataBefore = event.getCaseDataBefore();
            final DocumentUploaderType userType = event.getUserType();
            final Set<DocumentReference> documentReferences = new HashSet<>();
            final Set<DocumentInfo> documentInfos = new HashSet<>();

            Consumer<DocumentInfo> documentInfoConsumer = documentInfo -> {
                documentReferences.addAll(documentInfo.getDocumentReferences());
                documentInfos.add(documentInfo);
            };

            documentInfoConsumer.accept(getGeneralEvidence(caseData, caseDataBefore, userType));

            documentInfoConsumer.accept(getNewRespondentDocumentsUploaded(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getNewCorrespondenceDocumentsByHmtcs(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getNewCorrespondenceDocumentsByLA(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getNewCorrespondenceDocumentsBySolicitor(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getNewApplicationDocument(caseData,
                    caseDataBefore));

            if (!documentReferences.isEmpty()) {
                String documentTypes = documentInfos.stream()
                        .filter(documentInfo ->
                                !documentInfo.getDocumentReferences().isEmpty())
                        .flatMap(docs -> docs.getDocumentTypes().stream())
                        .map(docType -> String.join(" ", LIST, docType))
                        .collect(Collectors.joining("\n"));

                String subjectInfo = documentInfos.stream()
                        .filter(documentInfo ->
                                !documentInfo.getDocumentReferences().isEmpty())
                        .map(DocumentInfo::getDocumentType)
                        .findFirst().orElse("UNKNOWN");


                cafcassNotificationService.sendEmail(
                        caseData,
                        documentReferences,
                        NEW_DOCUMENT,
                        NewDocumentData.builder()
                                .documentTypes(documentTypes)
                                .emailSubjectInfo(subjectInfo)
                                .build()
                );
            }
        }

    }

    private DocumentInfo getNewApplicationDocument(CaseData caseData, CaseData caseDataBefore) {
        List<ApplicationDocument> newApplicationDocuments = unwrapElements(caseData.getApplicationDocuments());
        List<ApplicationDocument> oldApplicationDocuments = unwrapElements(caseDataBefore.getApplicationDocuments());

        List<ApplicationDocument> newlyAddedApplicationDocs = newApplicationDocuments.stream()
                .filter(newDoc -> !oldApplicationDocuments.contains(newDoc))
                .collect(toList());

        List<DocumentReference> documentReferences = newlyAddedApplicationDocs.stream()
                .map(ApplicationDocument::getDocument)
                .collect(toList());

        return newlyAddedApplicationDocs.stream()
                .map(ApplicationDocument::getDocumentType)
                .map(ApplicationDocumentType::getLabel)
                .collect(Collectors.collectingAndThen(toList(),
                    data ->
                        DocumentInfo.builder()
                                .documentReferences(documentReferences)
                                .documentTypes(data)
                                .documentType(FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                                .build())
                );
    }

    private DocumentInfo getGeneralEvidence(CaseData caseData, CaseData caseDataBefore, DocumentUploaderType userType) {
        var supportingEvidenceBundles = getDocuments(caseData,
            caseDataBefore,
            userType,
            (oldBundle, newDoc) -> !unwrapElements(oldBundle).contains(newDoc));

        var documentReferences = supportingEvidenceBundles.stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(Collectors.toList());

        return supportingEvidenceBundles.stream()
                .map(SupportingEvidenceBundle::getType)
                .filter(Objects::nonNull)
                .map(FurtherEvidenceType::getLabel)
                .collect(Collectors.collectingAndThen(toList(),
                    data ->
                        DocumentInfo.builder()
                            .documentReferences(documentReferences)
                            .documentTypes(data)
                            .documentType(FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                    .build())
                );
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

    private List<FurtherDocument> getNewUploadedFurtherDocuments(
        CaseData caseData, CaseData caseDataBefore,
        BiPredicate<List<FurtherDocument>, FurtherDocument> filter) {

        var newBundle = getFurtherDocumentsForMainApplication(caseData);
        var oldBundle = getFurtherDocumentsForMainApplication(caseDataBefore);

        List<FurtherDocument> newDocs = new ArrayList<>();

        newBundle.forEach(newDoc -> {
            if (filter.test(oldBundle, newDoc)) {
                newDocs.add(newDoc);
            }
        });
        return newDocs;
    }

    private List<String> getDocumentNames(List<FurtherDocument> documentBundle) {
        return documentBundle.stream().map(FurtherDocument::getName).collect(toList());
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

    private List<FurtherDocument> getFurtherDocumentsForMainApplication(CaseData caseData) {
        List<FurtherDocument> ret = new ArrayList<>();

        // Further application documents - for example the SWET or care plan
        List<Element<ApplicationDocument>> applicationDocuments = defaultIfNull(
            caseData.getApplicationDocuments(), List.of());
        ret.addAll(applicationDocuments.stream().map(Element::getValue).collect(toList()));

        // Respondent Statement
        List<Element<SupportingEvidenceBundle>> respondentStatements = defaultIfNull(
            getEvidenceBundleFromRespondentStatements(caseData), List.of());
        ret.addAll(respondentStatements.stream().map(Element::getValue).collect(toList()));

        // Any other documents
        List<Element<SupportingEvidenceBundle>> anyOtherDocsLA = defaultIfNull(
            caseData.getFurtherEvidenceDocumentsLA(), List.of());
        ret.addAll(anyOtherDocsLA.stream().map(Element::getValue).collect(toList()));

        List<Element<SupportingEvidenceBundle>> anyOtherDocsSol = defaultIfNull(
            caseData.getFurtherEvidenceDocumentsSolicitor(), List.of());
        ret.addAll(anyOtherDocsSol.stream().map(Element::getValue).collect(toList()));

        List<Element<SupportingEvidenceBundle>> anyOtherDocs = defaultIfNull(
            caseData.getFurtherEvidenceDocuments(), List.of());
        ret.addAll(anyOtherDocs.stream().map(Element::getValue).collect(toList()));

        List<Element<SupportingEvidenceBundle>> anyOtherDocsLinkedHearings = defaultIfNull(
            getEvidenceBundleFromHearings(caseData), List.of());
        ret.addAll(anyOtherDocsLinkedHearings.stream().map(Element::getValue).collect(toList()));

        return ret;
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundleFromHearings(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> evidenceBundle = new ArrayList<>();
        caseData.getHearingFurtherEvidenceDocuments().forEach(hearingFurtherDocuments ->
            evidenceBundle.addAll(hearingFurtherDocuments.getValue().getSupportingEvidenceBundle())
        );
        return evidenceBundle;
    }

    private List<Element<SupportingEvidenceBundle>> getEvidenceBundleFromRespondentStatements(CaseData caseData) {
        List<Element<SupportingEvidenceBundle>> evidenceBundle = new ArrayList<>();
        caseData.getRespondentStatements().forEach(statement ->
            evidenceBundle.addAll(statement.getValue().getSupportingEvidenceBundle())
        );
        return evidenceBundle;
    }

    private DocumentInfo getNewRespondentDocumentsUploaded(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> oldBundle = caseDataBefore.getRespondentStatements().stream()
                .flatMap(statement -> unwrapElements(statement.getValue().getSupportingEvidenceBundle()).stream())
                .collect(toList());
        List<SupportingEvidenceBundle> newBundle = caseData.getRespondentStatements().stream()
                .flatMap(statement -> unwrapElements(statement.getValue().getSupportingEvidenceBundle()).stream())
                .collect(toList());

        return getDocumentInfo(oldBundle, newBundle, "Respondent statement", FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION);
    }

    private DocumentInfo  getNewCorrespondenceDocumentsByHmtcs(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> oldBundle = unwrapElements(caseDataBefore.getCorrespondenceDocuments());
        List<SupportingEvidenceBundle> newBundle = unwrapElements(caseData.getCorrespondenceDocuments());

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getNewCorrespondenceDocumentsByLA(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> oldBundle = unwrapElements(caseDataBefore.getCorrespondenceDocumentsLA());
        List<SupportingEvidenceBundle> newBundle = unwrapElements(caseData.getCorrespondenceDocumentsLA());

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getNewCorrespondenceDocumentsBySolicitor(CaseData caseData, CaseData caseDataBefore) {
        List<SupportingEvidenceBundle> oldBundle = unwrapElements(caseDataBefore.getCorrespondenceDocumentsSolicitor());
        List<SupportingEvidenceBundle> newBundle = unwrapElements(caseData.getCorrespondenceDocumentsSolicitor());

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getDocumentInfo(List<SupportingEvidenceBundle> oldBundle,
                                         List<SupportingEvidenceBundle> newBundle,
                                         String documentType,
                                         String type) {
        return newBundle.stream()
                .filter(bundle -> !oldBundle.contains(bundle))
                .map(SupportingEvidenceBundle::getDocument)
                .collect(Collectors.collectingAndThen(toList(),
                    data -> DocumentInfo.builder()
                        .documentReferences(data)
                        .documentTypes(List.of(documentType))
                        .documentType(type)
                    .build())
                );
    }

    private List<Element<SupportingEvidenceBundle>> concatEvidenceBundles(List<Element<SupportingEvidenceBundle>> b1,
                                                                          List<Element<SupportingEvidenceBundle>> b2) {
        return Stream.concat(b1.stream(), b2.stream()).collect(toList());
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(DocumentUploadedEvent event) {
        furtherEvidenceDifferenceCalculator.calculate(event.getCaseData(), event.getCaseDataBefore())
            .forEach(bundle -> translationRequestService.sendRequest(event.getCaseData(),
                Optional.ofNullable(bundle.getValue().getTranslationRequirements()),
                bundle.getValue().getDocument(), bundle.getValue().asLabel())
            );
    }
}