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
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.DocumentInfo;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.ALL_LAS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CHILD_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.RESPONDENT_SOLICITOR;
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
public class FurtherEvidenceUploadedEventHandler {
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private final FurtherEvidenceUploadDifferenceCalculator furtherEvidenceDifferenceCalculator;
    private final TranslationRequestService translationRequestService;
    private final SendDocumentService sendDocumentService;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private static final String PDF = "pdf";
    private static final String LIST = "•";

    @EventListener
    public void sendDocumentsUploadedNotification(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        var newUploadedFurtherDocuments =
            getNotificationUserType2NewFurtherDocumentMap(caseData, caseDataBefore);


        if (!newUploadedFurtherDocuments.isEmpty()) {
            newUploadedFurtherDocuments.entrySet().forEach(entry -> {
                final Set<String> recipients = new LinkedHashSet<>();
                DocumentUploadNotificationUserType key = entry.getKey();

                if (key == CAFCASS) {
                    recipients.addAll(furtherEvidenceNotificationService.getCafcassEmails(caseData));
                } else if (key == CHILD_SOLICITOR) {
                    recipients.addAll(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData));
                } else if (key == RESPONDENT_SOLICITOR) {
                    recipients.addAll(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData));
                } else if (key == ALL_LAS) {
                    recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData));
                }

                if (isNotEmpty(recipients)) {
                    List<String> newDocumentNames = getDocumentNames(entry.getValue());
                    furtherEvidenceNotificationService.sendNotification(caseData, recipients, uploader.getFullName(),
                        newDocumentNames);
                }
            });
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
    public void sendCourtBundlesUploadedNotification(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        Map<String, Set<DocumentReference>> newCourtBundles = getNewCourtBundles(caseData, caseDataBefore);
        final Set<String> recipients = new HashSet<>();

        if (!newCourtBundles.isEmpty()) {
            recipients.addAll(furtherEvidenceNotificationService.getRepresentativeEmails(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData));
        }

        if (isNotEmpty(recipients)) {
            newCourtBundles
                .forEach((hearingDetails, value) ->
                    furtherEvidenceNotificationService.sendNotificationForCourtBundleUploaded(caseData, recipients,
                        hearingDetails));
        }
    }

    @EventListener
    public void sendCourtBundlesToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        final Optional<CafcassLookupConfiguration.Cafcass> recipientIsEngland =
                cafcassLookupConfiguration.getCafcassEngland(caseData.getCaseLocalAuthority());

        if (recipientIsEngland.isPresent()) {
            final CaseData caseDataBefore = event.getCaseDataBefore();
            Map<String, Set<DocumentReference>> newCourtBundles = getNewCourtBundles(caseData, caseDataBefore);
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
    public void sendDocumentsToCafcass(final FurtherEvidenceUploadedEvent event) {
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

            documentInfoConsumer.accept(getNewApplicationDocuments(caseData,
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

    private DocumentInfo getNewApplicationDocuments(CaseData caseData, CaseData caseDataBefore) {
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

    private List<Element<ApplicationDocument>> getNewApplicationDocuments(
        List<Element<ApplicationDocument>> applicationDocuments,
        List<Element<ApplicationDocument>> beforeApplicationDocuments) {
        List<Element<ApplicationDocument>> newApplicationDocuments = new ArrayList<>();
        defaultIfNull(applicationDocuments, new ArrayList<Element<ApplicationDocument>>()).forEach(newDoc -> {
            if (!defaultIfNull(beforeApplicationDocuments, List.of()).contains(newDoc)) {
                newApplicationDocuments.add(newDoc);
            }
        });
        return newApplicationDocuments;
    }

    private List<Element<SupportingEvidenceBundle>> getNewSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> beforeSupportingEvidenceBundle) {
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = new ArrayList<>();
        defaultIfNull(supportingEvidenceBundle, new ArrayList<Element<SupportingEvidenceBundle>>()).forEach(newDoc -> {
            if (!defaultIfNull(beforeSupportingEvidenceBundle, List.of()).contains(newDoc)) {
                newSupportingEvidenceBundle.add(newDoc);
            }
        });
        return newSupportingEvidenceBundle;
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

    private Map<String, Set<DocumentReference>> getNewCourtBundles(CaseData caseData, CaseData caseDataBefore) {
        List<CourtBundle> courtBundles = unwrapElements(caseData.getCourtBundleList());
        List<CourtBundle> oldCourtBundleList = unwrapElements(caseDataBefore.getCourtBundleList());

        return courtBundles.stream().filter(newDoc -> !oldCourtBundleList.contains(newDoc))
            .collect(groupingBy(CourtBundle::getHearing,
                mapping(CourtBundle::getDocument, toSet())));
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

    private Map<DocumentUploadNotificationUserType, List<FurtherDocument>>
        getNotificationUserType2NewFurtherDocumentMap(CaseData caseData, CaseData beforeCaseData) {

        // initialisation
        Map<DocumentUploadNotificationUserType, List<FurtherDocument>> ret = new HashMap<>();
        if (ret.get(ALL_LAS) == null) {
            ret.put(ALL_LAS, new ArrayList<>());
        }
        if (ret.get(CAFCASS) == null) {
            ret.put(CAFCASS, new ArrayList<>());
        }
        if (ret.get(CHILD_SOLICITOR) == null) {
            ret.put(CHILD_SOLICITOR, new ArrayList<>());
        }
        if (ret.get(RESPONDENT_SOLICITOR) == null) {
            ret.put(RESPONDENT_SOLICITOR, new ArrayList<>());
        }

        // Further application documents - for example the SWET or care plan
        // - everyone except respondent/child solicitors have permission to see
        // So we shouldn’t send the notification to respondent/child solicitors
        List<Element<ApplicationDocument>> newApplicationDocuments =
            getNewApplicationDocuments(caseData.getApplicationDocuments(), beforeCaseData.getApplicationDocuments());
        unwrapElements(newApplicationDocuments).forEach(applicationDocument -> {
            if (!applicationDocument.isConfidentialDocument()) {
                ret.get(CHILD_SOLICITOR).add(applicationDocument);
                ret.get(RESPONDENT_SOLICITOR).add(applicationDocument);
            }
            ret.get(ALL_LAS).add(applicationDocument);
        });

        // Respondent Statement
        List<Element<SupportingEvidenceBundle>> respondentStatements =
            getNewSupportingEvidenceBundle(
                getEvidenceBundleFromRespondentStatements(caseData),
                getEvidenceBundleFromRespondentStatements(beforeCaseData));
        unwrapElements(respondentStatements).forEach(respondentStatement -> {
            if (!respondentStatement.isConfidentialDocument()) {
                ret.get(CHILD_SOLICITOR).add(respondentStatement);
                ret.get(RESPONDENT_SOLICITOR).add(respondentStatement);
            }
            if (!(respondentStatement.isUploadedByHMCTS() && respondentStatement.isConfidentialDocument())) {
                ret.get(CAFCASS).add(respondentStatement);
                ret.get(ALL_LAS).add(respondentStatement);
            }
        });

        // Any other documents
        // Uploaded by LA
        List<Element<SupportingEvidenceBundle>> anyOtherDocsByLA =
            getNewSupportingEvidenceBundle(
                caseData.getFurtherEvidenceDocumentsLA(),
                beforeCaseData.getFurtherEvidenceDocumentsLA());
        unwrapElements(anyOtherDocsByLA).forEach(doc -> {
            if (!doc.isConfidentialDocument()) {
                ret.get(CHILD_SOLICITOR).add(doc);
                ret.get(RESPONDENT_SOLICITOR).add(doc);
            }
            ret.get(CAFCASS).add(doc);
            ret.get(ALL_LAS).add(doc);
        });
        // Uploaded by HMCTS Admin
        List<Element<SupportingEvidenceBundle>> anyOtherDocsByHmctsAdmin =
            getNewSupportingEvidenceBundle(
                caseData.getFurtherEvidenceDocuments(),
                beforeCaseData.getFurtherEvidenceDocuments());
        unwrapElements(anyOtherDocsByHmctsAdmin).forEach(doc -> {
            if (!doc.isConfidentialDocument()) {
                ret.get(CHILD_SOLICITOR).add(doc);
                ret.get(RESPONDENT_SOLICITOR).add(doc);
                ret.get(CAFCASS).add(doc);
                ret.get(ALL_LAS).add(doc);
            }
        });
        // Uploaded by Solicitor
        List<Element<SupportingEvidenceBundle>> anyOtherDocsBySolicitor =
            getNewSupportingEvidenceBundle(
                caseData.getFurtherEvidenceDocumentsSolicitor(),
                beforeCaseData.getFurtherEvidenceDocumentsSolicitor());
        unwrapElements(anyOtherDocsBySolicitor).forEach(doc -> {
            // no confidential document by solicitors
            ret.get(CHILD_SOLICITOR).add(doc);
            ret.get(RESPONDENT_SOLICITOR).add(doc);
            ret.get(CAFCASS).add(doc);
            ret.get(ALL_LAS).add(doc);
        });

        // Any Other Document From Hearings
        List<Element<SupportingEvidenceBundle>> anyOtherDocumentFromHearings =
            getNewSupportingEvidenceBundle(getEvidenceBundleFromHearings(caseData),
                getEvidenceBundleFromHearings(beforeCaseData));
        unwrapElements(anyOtherDocumentFromHearings).forEach(doc -> {
            if (!doc.isConfidentialDocument()) {
                ret.get(CHILD_SOLICITOR).add(doc);
                ret.get(RESPONDENT_SOLICITOR).add(doc);
            }
            if (!(doc.isUploadedByHMCTS() && doc.isConfidentialDocument())) {
                ret.get(CAFCASS).add(doc);
                ret.get(ALL_LAS).add(doc);
            }
        });

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
        defaultIfNull(caseData.getRespondentStatements(), new ArrayList<Element<RespondentStatement>>())
            .forEach(statement -> evidenceBundle.addAll(statement.getValue().getSupportingEvidenceBundle()));
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
    public void notifyTranslationTeam(FurtherEvidenceUploadedEvent event) {
        furtherEvidenceDifferenceCalculator.calculate(event.getCaseData(), event.getCaseDataBefore())
            .forEach(bundle -> translationRequestService.sendRequest(event.getCaseData(),
                Optional.ofNullable(bundle.getValue().getTranslationRequirements()),
                bundle.getValue().getDocument(), bundle.getValue().asLabel())
            );
    }
}