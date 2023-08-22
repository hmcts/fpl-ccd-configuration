package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDocument;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.SKELETON_ARGUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService.PDF;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsUploadedEventHandler {

    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassNotificationService cafcassNotificationService;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private final SendDocumentService sendDocumentService;

    private static final String LIST = "•";
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";
    public static final String ADDITIONAL_APPLICATIONS = "additional applications"; //TODO TBC obsolete in new flow?
    public static final Map<DocumentType, CafcassRequestEmailContentProvider> CAFCASS_EMAIL_CONTENT_MAP = Map.of(
        COURT_BUNDLE, CafcassRequestEmailContentProvider.COURT_BUNDLE,
        CASE_SUMMARY, CafcassRequestEmailContentProvider.CASE_SUMMARY,
        POSITION_STATEMENTS, CafcassRequestEmailContentProvider.POSITION_STATEMENT_RESPONDENT,
        SKELETON_ARGUMENTS, CafcassRequestEmailContentProvider.SKELETON_ARGUMENT
    );

    /**
     * Base on the confidential level configuration, send notification to the given recipient.
     * e.g.
     * Given: A CTSC level doc and a LA level doc are uploaded
     * Case 1: Both files are allowed to be sent to a CTSC level user
     * Case 2: Only LA level doc is allowed to be sent to a LA level user
     * Case 2: No doc is allowed to be sent to a non-confidential level user
     *
     * @param event the ManageDocumentsUploadedEvent
     */
    @Async
    @EventListener
    public void sendDocumentsUploadedNotification(final ManageDocumentsUploadedEvent event) {
        buildConfigurationMapGroupedByRecipient(event)
            .forEach((recipients, getConfidentialLevelFunction) -> {
                if (isNotEmpty(recipients)) {
                    List<Element<NotifyDocumentUploaded>> documentsToBeSent =
                        consolidateMapByConfiguration(event, getConfidentialLevelFunction)
                            .entrySet().stream()
                            .filter(entry -> !isHearingDocument(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .flatMap(List::stream)
                            .collect(toList());

                    if (!documentsToBeSent.isEmpty()) {
                        List<String> newDocumentNames = documentsToBeSent.stream()
                            .map(Element::getValue)
                            .map(NotifyDocumentUploaded::getNameForNotification)
                            .collect(toList());

                        if (!newDocumentNames.isEmpty()) {
                            furtherEvidenceNotificationService.sendNotification(event.getCaseData(), recipients,
                                event.getInitiatedBy().getFullName(), newDocumentNames);
                        }
                    }
                }
            });
    }

    // TODO unit test
    @Async
    @EventListener
    public void sendHearingDocumentsUploadedNotification(final ManageDocumentsUploadedEvent event) {
        buildConfigurationMapGroupedByRecipient(event)
            .forEach((recipients, getConfidentialLevelFunction) -> {
                List<HearingDocument> documentsToBeSent =
                    consolidateMapByConfiguration(event, getConfidentialLevelFunction)
                        .entrySet().stream()
                        .filter(entry -> isHearingDocument(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .map(Element::getValue)
                        .map(hearingDoc -> (HearingDocument) hearingDoc)
                        .collect(toList());

                if (!documentsToBeSent.isEmpty()) {
                    if (isNotEmpty(recipients)) {

                        Optional<HearingBooking> hearingBookings = event.getCaseData().getHearingDetails().stream()
                            .filter(element ->
                                element.getValue().toLabel().equals(documentsToBeSent.get(0).getHearing()))
                            .findFirst()
                            .map(Element::getValue);

                        List<String> newDocumentNames = documentsToBeSent.stream()
                            .map(doc -> doc.getDocument().getFilename()).collect(toList());
                        furtherEvidenceNotificationService.sendNotificationWithHearing(event.getCaseData(), recipients,
                            event.getInitiatedBy().getFullName(), newDocumentNames, hearingBookings);
                    }
                }
            });
    }

    @Async
    @EventListener
    public void sendDocumentsByPost(final ManageDocumentsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();
        DocumentUploaderType userType = event.getUploadedUserType();

        if (userType == SOLICITOR) {
            List<DocumentReference> nonConfidentialPdfDocumentsToBeSent =
                consolidateMapByConfiguration(event, (docType) -> ConfidentialLevel.NON_CONFIDENTIAL)
                    .entrySet().stream()
                    .filter(entry -> !isHearingDocument(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .map(NotifyDocumentUploaded::getDocument)
                    .filter(documentReference -> hasExtension(documentReference.getFilename(), PDF))
                    .collect(toList());

            if (!nonConfidentialPdfDocumentsToBeSent.isEmpty()) {
                Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));
                sendDocumentService.sendDocuments(caseData, nonConfidentialPdfDocumentsToBeSent,
                    new ArrayList<>(allRecipients));
            }
        }
    }

    @Retryable(value = EmailFailedSendException.class)
    @Async
    @EventListener
    public void sendDocumentsToCafcass(final ManageDocumentsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            Map<DocumentType, List<Element<NotifyDocumentUploaded>>> documentsToBeSent =
                consolidateMapByConfiguration(event, DocumentUploadedNotificationConfiguration::getSendToCafcassEngland)
                    .entrySet().stream()
                    .filter(entry -> !isHearingDocument(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!documentsToBeSent.isEmpty()) {
                String documentTypes = documentsToBeSent.keySet().stream()
                    .map(docType -> String.join(" ", LIST, docType.getDescription()))
                    .collect(joining("\n"));

                String subjectInfo = documentsToBeSent.keySet().stream()
                    .map(documentType -> (COURT_CORRESPONDENCE.equals(documentType)) ?
                        CORRESPONDENCE : FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                    .findFirst().orElse("UNKNOWN");

                cafcassNotificationService.sendEmail(
                    caseData,
                    getDocumentReferences(documentsToBeSent),
                    CafcassRequestEmailContentProvider.NEW_DOCUMENT,
                    NewDocumentData.builder()
                        .documentTypes(documentTypes)
                        .emailSubjectInfo(subjectInfo)
                        .build()
                );
            }
        }
    }

    @Retryable(value = EmailFailedSendException.class)
    @Async
    @EventListener
    public void sendHearingDocumentsToCafcass(final ManageDocumentsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            // 1. get available new hearing documents
            consolidateMapByConfiguration(event, DocumentUploadedNotificationConfiguration::getSendToCafcassEngland)
                .entrySet().stream()
                .filter(entry -> isHearingDocument(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((docType, hearingDocuments) ->
                    // 2. for each hearing document type, group by hearing
                    hearingDocuments.stream()
                        .map(Element::getValue)
                        .map(hearingDocument -> (HearingDocument) hearingDocument)
                        .collect(groupingBy(HearingDocument::getHearing,
                            mapping(HearingDocument::getDocument, toSet())))
                        .forEach((hearing, doc) ->
                            // 3. for each hearing document type and hearing, send to Cafcass
                            cafcassNotificationService.sendEmail(
                                caseData,
                                doc,
                                CAFCASS_EMAIL_CONTENT_MAP.get(docType),
                                CourtBundleData.builder()
                                    .hearingDetails(hearing)
                                    .build())));
        }
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(final ManageDocumentsUploadedEvent event) {
        // TODO no translation in new Manage doc flow?

    }

    private Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>> buildConfigurationMapGroupedByRecipient(final ManageDocumentsUploadedEvent event) {

        final CaseData caseData = event.getCaseData();

        Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>> resultMap =
            new HashMap<>();

        // designated LA
        resultMap.put(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToDesignatedLA);

        // secondary LA
        resultMap.put(furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToSecondaryLA);

        // legal representative
        resultMap.put(furtherEvidenceNotificationService.getLegalRepresentativeOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToLegalRepresentative);

        // cafcass representative
        resultMap.put(furtherEvidenceNotificationService.getCafcassRepresentativeEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToCafcassRepresentative);

        // respondent solicitor
        resultMap.put(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor);

        // child solicitor
        resultMap.put(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToChildSolicitor);

        return resultMap;
    }

    private boolean isHearingDocument(DocumentType documentType) {
        return COURT_BUNDLE.equals(documentType) || CASE_SUMMARY.equals(documentType)
               || POSITION_STATEMENTS.equals(documentType) || SKELETON_ARGUMENTS.equals(documentType);
    }

    /**
     * Base on the notification configuration of each file type, returns all files uploaded in the event which satisfy the confidential level configured.
     * e.g.
     * Given: A user has CTSC level over DocumentType A and non-confidential level over DocumentType B.
     * If any, this method will return all newly uploaded documents of DocumentType A and only non-confidential documents of DocumentType B.
     *
     * @param event the ManageDocumentsUploadedEvent
     * @param getConfidentialLevelFunction A function accepts the notification configuration of a document type and
     *                                     returns the confidential level of the targeted user type on that document type.
     *                                     e.g. To get the confidential level of respondent solicitor
     *                                         DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor
     * @return Lists of documents grouped by document type
     */
    private Map<DocumentType, List<Element<NotifyDocumentUploaded>>> consolidateMapByConfiguration(ManageDocumentsUploadedEvent event,
                                                                                                   Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel> getConfidentialLevelFunction) {

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> nonConfidentialDocuments =
            event.getNewDocuments().entrySet().stream()
            .filter(entry -> getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration()) != null)
            .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> laLevelDocuments =
            event.getNewDocumentsLA().entrySet().stream()
                .filter(entry -> {
                    ConfidentialLevel levelConfiguration =
                        getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration());
                    return ConfidentialLevel.LA.equals(levelConfiguration)
                           || ConfidentialLevel.CTSC.equals(levelConfiguration);
                })
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> ctscLevelDocuments =
            event.getNewDocumentsLA().entrySet().stream()
                .filter(entry -> ConfidentialLevel.CTSC
                    .equals(getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration())))
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));


        return Stream.of(nonConfidentialDocuments, laLevelDocuments, ctscLevelDocuments)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));
    }

    private Set<DocumentReference> getDocumentReferences(
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> documentsMap) {

        return documentsMap.values().stream().flatMap(List::stream).collect(toList()).stream()
            .map(Element::getValue).map(NotifyDocumentUploaded::getDocument)
            .collect(toSet());
    }
}
