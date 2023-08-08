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
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.SKELETON_ARGUMENTS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsUploadedEventHandler {

    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassNotificationService cafcassNotificationService;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    private static final String LIST = "•";
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";
    public static final String ADDITIONAL_APPLICATIONS = "additional applications"; //TODO TBC obsolete in new flow?

    @Async
    @EventListener
    public void sendDocumentsUploadedNotification(final ManageDocumentsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        // send to designated LA
        sendNotification(event,
            furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToDesignatedLA);

        // send to secondary LA
        sendNotification(event,
            furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToSecondaryLA);


        // send to legal representative
        sendNotification(event,
            furtherEvidenceNotificationService.getLegalRepresentativeOnly(caseData),
            DocumentUploadedNotificationConfiguration::getSendToLegalRepresentative);

        // send to cafcass representative
        sendNotification(event,
            furtherEvidenceNotificationService.getCafcassRepresentativeEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToCafcassRepresentative);

        // send to respondent solicitor
        sendNotification(event,
            furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor);

        // send to child solicitor
        sendNotification(event,
            furtherEvidenceNotificationService.getChildSolicitorEmails(caseData),
            DocumentUploadedNotificationConfiguration::getSendToChildSolicitor);
    }

    /**
     * Base on the confidential level configuration, send notification to the given recipient.
     * e.g.
     * Given: A CTSC level doc and a LA level doc are uploaded
     * Case 1: Both files are allowed to be sent to a CTSC level user
     * Case 2: Only LA level doc is allowed to be sent to a LA level user
     * Case 2: No doc is allowed to be sent to a non-confidential level user
     *
     * @param event the ManageDocumentsUploadedEvent
     * @param recipients the recipients to be sent
     * @param getConfidentialLevelFunction A function accepts the notification configuration of a document type and
     *                                     returns the confidential level of the targeted user type on that document type.
     *                                     e.g. To get the confidential level of respondent solicitor
     *                                          DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor
     */
    private void sendNotification(final ManageDocumentsUploadedEvent event, Set<String> recipients,
            Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel> getConfidentialLevelFunction) {

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> documentsToBeSent =
            consolidateMapByConfiguration(event, getConfidentialLevelFunction)
                .entrySet().stream()
                .filter(entry -> !isHearingDocument(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!documentsToBeSent.isEmpty()) {
            if (isNotEmpty(recipients)) {
                List<String> newDocumentNames = documentsToBeSent.values().stream().flatMap(List::stream)
                    .map(Element::getValue)
                    .map(NotifyDocumentUploaded::getDocument)
                    .map(DocumentReference::getFilename)
                    .collect(toList());

                if (!newDocumentNames.isEmpty()) {
                    furtherEvidenceNotificationService.sendNotification(event.getCaseData(), recipients,
                        event.getInitiatedBy().getFullName(), newDocumentNames);
                }
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
                NEW_DOCUMENT,
                NewDocumentData.builder()
                    .documentTypes(documentTypes)
                    .emailSubjectInfo(subjectInfo)
                    .build()
            );
        }
    }

    @Retryable(value = EmailFailedSendException.class)
    @Async
    @EventListener
    public void sendHearingDocumentsToCafcass(final ManageDocumentsUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            // TODO
        }
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
    private Map<DocumentType, List<Element<NotifyDocumentUploaded>>> consolidateMapByConfiguration(
            ManageDocumentsUploadedEvent event,
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
