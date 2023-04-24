package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocument;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.DocumentInfo;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.ALL_LAS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CAFCASS_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CHILD_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_CHILD;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_RESPONDENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.SKELETON_ARGUMENT;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FurtherEvidenceUploadedEventHandler {
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";
    public static final String ADDITIONAL_APPLICATIONS = "additional applications";
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

        newUploadedFurtherDocuments.entrySet().forEach(entry -> {
            final Set<String> recipients = new LinkedHashSet<>();
            if (!entry.getValue().isEmpty()) {
                DocumentUploadNotificationUserType key = entry.getKey();
                if (key == CAFCASS_REPRESENTATIVES) {
                    recipients.addAll(furtherEvidenceNotificationService.getCafcassRepresentativeEmails(caseData));
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
            }
        });
    }

    @EventListener
    public void sendDocumentsByPost(final FurtherEvidenceUploadedEvent event) {
        DocumentUploaderType userType = event.getUserType();

        if (userType == SOLICITOR) {
            final CaseData caseData = event.getCaseData();
            final CaseData caseDataBefore = event.getCaseDataBefore();

            var newNonConfidentialDocuments = getNewDocuments(caseData,
                caseDataBefore, userType, newDoc -> !newDoc.isConfidentialDocument(), true);

            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));
            // filter document with PDF file extension
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

        Predicate<Map.Entry<String, Set<DocumentReference>>> predicate = not(entry -> entry.getValue().isEmpty());

        if (newCourtBundles.entrySet().stream().anyMatch(predicate)) {
            recipients.addAll(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData));
        }

        if (isNotEmpty(recipients)) {
            newCourtBundles.entrySet().stream()
                .filter(predicate)
                .forEach(entry ->
                    furtherEvidenceNotificationService.sendNotificationForCourtBundleUploaded(caseData, recipients,
                        entry.getKey()));
        }
    }

    @EventListener
    public void sendHearingDocumentsUploadedNotification(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();
        final UserDetails uploader = event.getInitiatedBy();

        List<HearingDocument> newHearingDocuments = getNewHearingDocuments(
            caseData.getHearingDocuments().getCaseSummaryList(),
            caseDataBefore.getHearingDocuments().getCaseSummaryList());
        newHearingDocuments.addAll(getNewHearingDocuments(
            caseData.getHearingDocuments().getPositionStatementChildListV2(),
            caseDataBefore.getHearingDocuments().getPositionStatementChildListV2()));
        newHearingDocuments.addAll(getNewHearingDocuments(
            caseData.getHearingDocuments().getPositionStatementRespondentListV2(),
            caseDataBefore.getHearingDocuments().getPositionStatementRespondentListV2()));
        newHearingDocuments.addAll(getNewHearingDocuments(
            caseData.getHearingDocuments().getSkeletonArgumentList(),
            caseDataBefore.getHearingDocuments().getSkeletonArgumentList()));

        if (!newHearingDocuments.isEmpty()) {
            final Set<String> recipients = new LinkedHashSet<>();
            recipients.addAll(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData));
            recipients.addAll(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData));

            if (isNotEmpty(recipients)) {
                Optional<HearingBooking> hearingBookings = caseData.getHearingDetails().stream()
                    .filter(element -> element.getValue().toLabel().equals(newHearingDocuments.get(0).getHearing()))
                    .findFirst()
                    .map(Element::getValue);

                List<String> newDocumentNames = newHearingDocuments.stream()
                    .map(doc -> doc.getDocument().getFilename()).collect(toList());
                furtherEvidenceNotificationService.sendNotificationWithHearing(caseData, recipients,
                    uploader.getFullName(), newDocumentNames, hearingBookings);
            }
        }
    }

    @EventListener
    public void sendHearingDocumentsToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            List<HearingDocument> newCaseSummaries = getNewHearingDocuments(
                caseData.getHearingDocuments().getCaseSummaryList(),
                caseDataBefore.getHearingDocuments().getCaseSummaryList());
            sendHearingDocumentsToCafcass(caseData, newCaseSummaries, CASE_SUMMARY);

            List<HearingDocument> newPositionStatementChildren =
                getNewHearingDocuments(
                    caseData.getHearingDocuments().getPositionStatementChildListV2(),
                    caseDataBefore.getHearingDocuments().getPositionStatementChildListV2());
            sendHearingDocumentsToCafcass(caseData, newPositionStatementChildren, POSITION_STATEMENT_CHILD);

            List<HearingDocument> newPositionStatementRespondents =
                getNewHearingDocuments(
                    caseData.getHearingDocuments().getPositionStatementRespondentListV2(),
                    caseDataBefore.getHearingDocuments().getPositionStatementRespondentListV2());
            sendHearingDocumentsToCafcass(caseData, newPositionStatementRespondents, POSITION_STATEMENT_RESPONDENT);

            List<HearingDocument> newSkeletonArgument =
                getNewHearingDocuments(
                    caseData.getHearingDocuments().getSkeletonArgumentList(),
                    caseDataBefore.getHearingDocuments().getSkeletonArgumentList());
            sendHearingDocumentsToCafcass(caseData, newSkeletonArgument, SKELETON_ARGUMENT);
        }
    }

    private void sendHearingDocumentsToCafcass(CaseData caseData, List<HearingDocument> newHearingDocuments,
                                               CafcassRequestEmailContentProvider provider) {
        Map<String, Set<DocumentReference>> newHearingDocs = newHearingDocuments.stream()
            .collect(groupingBy(HearingDocument::getHearing,
                mapping(HearingDocument::getDocument, toSet())));

        newHearingDocs.forEach((hearing, doc) ->
            cafcassNotificationService.sendEmail(
                    caseData,
                    doc,
                    provider,
                    CourtBundleData.builder()
                        .hearingDetails(hearing)
                        .build()));
    }

    @EventListener
    public void sendCourtBundlesToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            final CaseData caseDataBefore = event.getCaseDataBefore();

            Map<String, Set<DocumentReference>> newCourtBundles = getNewCourtBundles(caseData, caseDataBefore);

            newCourtBundles
                    .forEach((key, value) -> {
                        if (value != null && !value.isEmpty()) {
                            cafcassNotificationService.sendEmail(
                                    caseData,
                                    value,
                                    COURT_BUNDLE,
                                    CourtBundleData.builder()
                                            .hearingDetails(key)
                                            .build()
                            );
                        }
                    });
        }
    }

    @EventListener
    public void sendDocumentsToCafcass(final FurtherEvidenceUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            final CaseData caseDataBefore = event.getCaseDataBefore();
            final DocumentUploaderType userType = event.getUserType();
            final Set<DocumentReference> documentReferences = new HashSet<>();
            final Set<DocumentInfo> documentInfos = new HashSet<>();

            Consumer<DocumentInfo> documentInfoConsumer = documentInfo -> {
                documentReferences.addAll(documentInfo.getDocumentReferences());
                documentInfos.add(documentInfo);
            };

            documentInfoConsumer.accept(getNewGeneralEvidence(caseData, caseDataBefore, userType));

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

            documentInfoConsumer.accept(getHearingFurtherEvidenceDocuments(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getOtherApplicationBundle(caseData,
                    caseDataBefore));

            documentInfoConsumer.accept(getC2DocumentBundle(caseData,
                    caseDataBefore));

            if (!documentReferences.isEmpty()) {
                String documentTypes = documentInfos.stream()
                        .filter(documentInfo ->
                                !documentInfo.getDocumentReferences().isEmpty())
                        .flatMap(docs -> docs.getDocumentTypes().stream())
                        .map(docType -> String.join(" ", LIST, docType))
                        .collect(joining("\n"));

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

    private DocumentInfo getOtherApplicationBundle(CaseData caseData, CaseData caseDataBefore) {
        Set<DocumentReference> oldDocumentReferences = unwrapElements(
                    caseDataBefore.getAdditionalApplicationsBundle()
                ).stream()
                .map(AdditionalApplicationsBundle::getOtherApplicationsBundle)
                .filter(Objects::nonNull)
                .map(OtherApplicationsBundle::getAllDocumentReferences)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Element::getValue)
                .collect(toSet());

        return unwrapElements(caseData.getAdditionalApplicationsBundle()).stream()
                .map(AdditionalApplicationsBundle::getOtherApplicationsBundle)
                .filter(Objects::nonNull)
                .map(OtherApplicationsBundle::getAllDocumentReferences)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Element::getValue)
                .filter(not(oldDocumentReferences::contains))
                .map(documentRef -> {
                    documentRef.setType(ADDITIONAL_APPLICATIONS);
                    return documentRef;
                })
                .collect(collectingAndThen(toList(),
                    data -> DocumentInfo.builder()
                        .documentReferences(data)
                        .documentTypes(data.stream()
                                .map(DocumentReference::getType)
                                .collect(toList()))
                        .documentType(ADDITIONAL_APPLICATIONS)
                        .build())
                );
    }

    private DocumentInfo getC2DocumentBundle(CaseData caseData, CaseData caseDataBefore) {
        Set<DocumentReference> oldDocumentReferences = unwrapElements(
                    caseDataBefore.getAdditionalApplicationsBundle()
                ).stream()
                .map(AdditionalApplicationsBundle::getC2DocumentBundle)
                .filter(Objects::nonNull)
                .map(C2DocumentBundle::getAllC2DocumentReferences)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Element::getValue)
                .collect(toSet());

        return unwrapElements(caseData.getAdditionalApplicationsBundle()).stream()
                .map(AdditionalApplicationsBundle::getC2DocumentBundle)
                .filter(Objects::nonNull)
                .map(C2DocumentBundle::getAllC2DocumentReferences)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Element::getValue)
                .filter(not(oldDocumentReferences::contains))
                .map(documentRef -> {
                    documentRef.setType(ADDITIONAL_APPLICATIONS);
                    return documentRef;
                })
                .collect(collectingAndThen(toList(),
                    data -> DocumentInfo.builder()
                        .documentReferences(data)
                        .documentTypes(data.stream()
                                .map(DocumentReference::getType)
                                .collect(toList()))
                        .documentType(ADDITIONAL_APPLICATIONS)
                        .build())
                );
    }

    private DocumentInfo getHearingFurtherEvidenceDocuments(CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> newAnyOtherDocumentFromHearings =
            getNewSupportingEvidenceBundle(getEvidenceBundleFromHearings(caseData),
                getEvidenceBundleFromHearings(caseDataBefore));

        return newAnyOtherDocumentFromHearings.stream()
                .map(Element::getValue)
                .filter(bundle -> !NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(bundle.getType()))
                .map(supportingEvidenceBundle -> {
                    DocumentReference document = supportingEvidenceBundle.getDocument();
                    document.setType(Optional.ofNullable(supportingEvidenceBundle.getType())
                            .map(FurtherEvidenceType::getLabel)
                            .orElse(supportingEvidenceBundle.getName()));
                    return document;
                })
                .collect(collectingAndThen(toList(),
                    data -> DocumentInfo.builder()
                                .documentReferences(data)
                                .documentTypes(data.stream()
                                        .map(DocumentReference::getType)
                                        .collect(toList()))
                                .documentType(FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                            .build())
                );
    }

    private <T extends WithDocument> boolean hasNewDocumentUploaded(List<Element<T>> existingElements,
                                                                    Element<T> test) {
        Assert.notNull(test.getId(), "id is required to determine change of uploaded document");
        Optional<Element<T>> hitElement = ElementUtils.findElement(test.getId(), defaultIfNull(existingElements,
            List.of()));
        if (!hitElement.isPresent()) {
            return true;
        } else {
            return !Optional.ofNullable(test.getValue().getDocument()).orElse(DocumentReference.builder().build())
                .equals(Optional.ofNullable(hitElement.get().getValue().getDocument())
                    .orElse(DocumentReference.builder().build()));
        }
    }

    private DocumentInfo getNewApplicationDocuments(CaseData caseData, CaseData caseDataBefore) {
        Set<ApplicationDocument> newlyAddedApplicationDocs =
            unwrapElements(getNewApplicationDocuments(caseData.getApplicationDocuments(),
                caseDataBefore.getApplicationDocuments())).stream()
                .collect(toSet());

        return newlyAddedApplicationDocs.stream()
                .map(applicationDocument -> {
                    DocumentReference document = applicationDocument.getDocument();
                    document.setType(Optional.ofNullable(applicationDocument.getDocumentType())
                            .map(ApplicationDocumentType::getLabel)
                            .orElse(applicationDocument.getDocumentName()));
                    return document;
                })
                .collect(collectingAndThen(toList(),
                    data ->
                        DocumentInfo.builder()
                            .documentReferences(data)
                            .documentTypes(data.stream()
                                    .map(DocumentReference::getType)
                                    .collect(toList()))
                            .documentType(FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                            .build())
                );
    }

    private List<Element<ApplicationDocument>> getNewApplicationDocuments(
        List<Element<ApplicationDocument>> applicationDocuments,
        List<Element<ApplicationDocument>> beforeApplicationDocuments) {
        List<Element<ApplicationDocument>> newApplicationDocuments = new ArrayList<>();
        defaultIfNull(applicationDocuments, new ArrayList<Element<ApplicationDocument>>()).forEach(newDoc -> {
            if (hasNewDocumentUploaded(beforeApplicationDocuments, newDoc)) {
                newApplicationDocuments.add(newDoc);
            }
        });
        return newApplicationDocuments;
    }

    private List<Element<SupportingEvidenceBundle>> getNewSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> beforeSupportingEvidenceBundle) {
        return getNewSupportingEvidenceBundle(supportingEvidenceBundle, beforeSupportingEvidenceBundle, null);
    }

    private List<Element<SupportingEvidenceBundle>> getNewSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        List<Element<SupportingEvidenceBundle>> beforeSupportingEvidenceBundle,
        Predicate<SupportingEvidenceBundle> additionalPredicate) {
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = new ArrayList<>();
        defaultIfNull(supportingEvidenceBundle, new ArrayList<Element<SupportingEvidenceBundle>>()).forEach(newDoc -> {
            if (hasNewDocumentUploaded(beforeSupportingEvidenceBundle, newDoc)
                && Optional.ofNullable(additionalPredicate).orElse((x) -> true)
                .test(newDoc.getValue())) {
                newSupportingEvidenceBundle.add(newDoc);
            }
        });
        return newSupportingEvidenceBundle;
    }

    private DocumentInfo getNewGeneralEvidence(CaseData caseData, CaseData caseDataBefore,
                                               DocumentUploaderType userType) {
        var supportingEvidenceBundles = getNewDocuments(caseData,
            caseDataBefore,
            userType,
            null);

        return supportingEvidenceBundles.stream()
                .filter(bundle -> !NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(bundle.getType()))
                .map(bundle -> {
                    DocumentReference document = bundle.getDocument();
                    document.setType(Optional.ofNullable(bundle.getType())
                            .map(FurtherEvidenceType::getLabel)
                            .orElse(bundle.getName()));
                    return document;
                })
                .collect(collectingAndThen(toList(),
                    data ->
                        DocumentInfo.builder()
                            .documentReferences(data)
                            .documentTypes(data.stream()
                                    .map(DocumentReference::getType)
                                    .collect(toList()))
                            .documentType(FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                            .build()
                        )
                );
    }

    @SuppressWarnings("unchecked")
    private <T extends HearingDocument> List<HearingDocument> getNewHearingDocuments(List<Element<T>> documents,
                                                                                     List<Element<T>> documentsBefore) {
        List<Element<T>> newHearingDocuments = new ArrayList<>();
        defaultIfNull(documents, new ArrayList<Element<T>>()).forEach(newDoc -> {
            if (hasNewDocumentUploaded(documentsBefore, newDoc)) {
                newHearingDocuments.add(newDoc);
            }
        });
        return (List<HearingDocument>) unwrapElements(newHearingDocuments);
    }

    private Map<String, Set<DocumentReference>> getNewCourtBundles(CaseData caseData, CaseData caseDataBefore) {
        Map<String, List<CourtBundle>> oldMapOfCourtBundles =
            unwrapElements(caseDataBefore.getHearingDocuments().getCourtBundleListV2()).stream()
                .collect(
                    groupingBy(HearingCourtBundle::getHearing,
                        flatMapping(courtBundle -> unwrapElements(courtBundle.getCourtBundle()).stream(),
                            toList())));

        return unwrapElements(caseData.getHearingDocuments().getCourtBundleListV2()).stream()
                .collect(
                    groupingBy(HearingCourtBundle::getHearing,
                        flatMapping(courtBundle -> {
                            List<CourtBundle> bundles = unwrapElements(courtBundle.getCourtBundle());
                            List<CourtBundle> oldBundles =
                                Optional.ofNullable(oldMapOfCourtBundles.get(courtBundle.getHearing()))
                                    .orElse(Collections.emptyList());

                            List<CourtBundle> filteredBundle = new ArrayList<>(bundles);
                            filteredBundle.removeAll(oldBundles);
                            return filteredBundle.stream().map(CourtBundle::getDocument)
                                .collect(toSet())
                                .stream();
                        }, toSet())));
    }

    private List<SupportingEvidenceBundle> getNewDocuments(
        CaseData caseData, CaseData caseDataBefore,
        DocumentUploaderType userType,
        Predicate<SupportingEvidenceBundle> additionalPredicate) {
        return getNewDocuments(caseData, caseDataBefore, userType, additionalPredicate, false);
    }

    private List<SupportingEvidenceBundle> getNewDocuments(
        CaseData caseData, CaseData caseDataBefore,
        DocumentUploaderType userType,
        Predicate<SupportingEvidenceBundle> additionalPredicate, boolean concatRespondentStatement) {
        return unwrapElements(getNewSupportingEvidenceBundle(
            getEvidenceBundle(caseData, userType, concatRespondentStatement),
            getEvidenceBundle(caseDataBefore, userType, concatRespondentStatement), additionalPredicate));
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
                                                                      DocumentUploaderType uploaderType,
                                                                      boolean concatRespondentStatement) {
        if (uploaderType == DESIGNATED_LOCAL_AUTHORITY || uploaderType == SECONDARY_LOCAL_AUTHORITY) {
            return caseData.getFurtherEvidenceDocumentsLA();
        }  else if (uploaderType == SOLICITOR) {
            List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle =
                defaultIfNull(caseData.getFurtherEvidenceDocumentsSolicitor(), List.of());
            if (concatRespondentStatement) {
                List<Element<SupportingEvidenceBundle>> respondentStatementsBundle =
                    getEvidenceBundleFromRespondentStatements(caseData);
                return concatEvidenceBundles(furtherEvidenceBundle, respondentStatementsBundle);
            } else {
                return furtherEvidenceBundle;
            }
        } else {
            return caseData.getFurtherEvidenceDocuments();
        }
    }

    private Map<DocumentUploadNotificationUserType, List<FurtherDocument>>
        getNotificationUserType2NewFurtherDocumentMap(CaseData caseData, CaseData beforeCaseData) {

        // initialisation
        Map<DocumentUploadNotificationUserType, List<FurtherDocument>> ret = new HashMap<>();
        ret.put(ALL_LAS, new ArrayList<>());
        ret.put(CAFCASS_REPRESENTATIVES, new ArrayList<>());
        ret.put(CHILD_SOLICITOR, new ArrayList<>());
        ret.put(RESPONDENT_SOLICITOR, new ArrayList<>());

        // Further application documents - for example the SWET or care plan
        // - everyone except respondent/child solicitors have permission to see
        // So we shouldn’t send the notification to respondent/child solicitors
        // DFPL-1087, respondent/child should receive notification if the docs are not confidential
        List<Element<ApplicationDocument>> newApplicationDocuments =
            getNewApplicationDocuments(caseData.getApplicationDocuments(), beforeCaseData.getApplicationDocuments());
        unwrapElements(newApplicationDocuments).forEach(applicationDocument -> {
            ret.get(ALL_LAS).add(applicationDocument);
            if (!applicationDocument.isConfidentialDocument()) {
                ret.get(CAFCASS_REPRESENTATIVES).add(applicationDocument);
                ret.get(CHILD_SOLICITOR).add(applicationDocument);
                ret.get(RESPONDENT_SOLICITOR).add(applicationDocument);
            }
        });

        // Respondent Statement
        List<Element<SupportingEvidenceBundle>> respondentStatements =
            getNewSupportingEvidenceBundle(
                getEvidenceBundleFromRespondentStatements(caseData),
                getEvidenceBundleFromRespondentStatements(beforeCaseData));
        unwrapElements(respondentStatements).forEach(respondentStatement -> {
            if (!respondentStatement.isConfidentialDocument()) {
                ret.get(CAFCASS_REPRESENTATIVES).add(respondentStatement);
                ret.get(CHILD_SOLICITOR).add(respondentStatement);
                ret.get(RESPONDENT_SOLICITOR).add(respondentStatement);
            }
            if (!(respondentStatement.isUploadedByHMCTS() && respondentStatement.isConfidentialDocument())) {
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
                if (!NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(doc.getType())) {
                    ret.get(CAFCASS_REPRESENTATIVES).add(doc);
                }
            }
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
                if (!NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(doc.getType())) {
                    ret.get(CAFCASS_REPRESENTATIVES).add(doc);
                }
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
            if (!NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(doc.getType())) {
                ret.get(CAFCASS_REPRESENTATIVES).add(doc);
            }
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
                if (!NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE.equals(doc.getType())) {
                    ret.get(CAFCASS_REPRESENTATIVES).add(doc);
                }
            }
            if (!(doc.isUploadedByHMCTS() && doc.isConfidentialDocument())) {
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
        return getDocumentInfo(getEvidenceBundleFromRespondentStatements(caseDataBefore),
            getEvidenceBundleFromRespondentStatements(caseData),
            "Respondent statement", FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION);
    }

    private DocumentInfo getNewCorrespondenceDocumentsByHmtcs(CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> oldBundle = caseDataBefore.getCorrespondenceDocuments();
        List<Element<SupportingEvidenceBundle>> newBundle = caseData.getCorrespondenceDocuments();

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getNewCorrespondenceDocumentsByLA(CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> oldBundle = caseDataBefore.getCorrespondenceDocumentsLA();
        List<Element<SupportingEvidenceBundle>> newBundle = caseData.getCorrespondenceDocumentsLA();

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getNewCorrespondenceDocumentsBySolicitor(CaseData caseData, CaseData caseDataBefore) {
        List<Element<SupportingEvidenceBundle>> oldBundle = caseDataBefore.getCorrespondenceDocumentsSolicitor();
        List<Element<SupportingEvidenceBundle>> newBundle = caseData.getCorrespondenceDocumentsSolicitor();

        return getDocumentInfo(oldBundle, newBundle, CORRESPONDENCE, CORRESPONDENCE);
    }

    private DocumentInfo getDocumentInfo(List<Element<SupportingEvidenceBundle>> oldBundle,
                                         List<Element<SupportingEvidenceBundle>> newBundle,
                                         String documentType,
                                         String type) {
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle = new ArrayList<>();
        defaultIfNull(newBundle, new ArrayList<Element<SupportingEvidenceBundle>>()).forEach(newDoc -> {
            if (hasNewDocumentUploaded(oldBundle, newDoc)) {
                newSupportingEvidenceBundle.add(newDoc);
            }
        });
        return unwrapElements(newSupportingEvidenceBundle).stream()
                .map(bundle -> {
                    DocumentReference document = bundle.getDocument();
                    document.setType(
                            Optional.ofNullable(bundle.getType())
                                    .map(FurtherEvidenceType::getLabel)
                                    .orElse(documentType)
                    );
                    return document;
                })
                .collect(collectingAndThen(toList(),
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
