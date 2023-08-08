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
     * Send notification to the given recipient.
     * @param event the ManageDocumentsUploadedEvent
     * @param recipients the recipients to be sent
     * @param getConfidentialLevelFunction A function returning the corresponding confidential level of the user type
     *                                     e.g. To get the confidential level of respondent solicitor
     *                                     (documentUploadedNotificationConfiguration) ->
     *                                         documentUploadedNotificationConfiguration.sendToRespondentSolicitor()
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

    private Map<DocumentType, List<Element<NotifyDocumentUploaded>>> consolidateMapByConfiguration(
            ManageDocumentsUploadedEvent event,
            Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel> getConfidentialLevel) {

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> nonConfidentialDocuments =
            event.getNewDocuments().entrySet().stream()
            .filter(entry -> getConfidentialLevel.apply(entry.getKey().getNotificationConfiguration()) != null)
            .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> laLevelDocuments =
            event.getNewDocumentsLA().entrySet().stream()
                .filter(entry -> {
                    ConfidentialLevel levelConfiguration =
                        getConfidentialLevel.apply(entry.getKey().getNotificationConfiguration());
                    return ConfidentialLevel.LA.equals(levelConfiguration)
                           || ConfidentialLevel.CTSC.equals(levelConfiguration);
                })
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> ctscLevelDocuments =
            event.getNewDocumentsLA().entrySet().stream()
                .filter(entry -> ConfidentialLevel.CTSC
                    .equals(getConfidentialLevel.apply(entry.getKey().getNotificationConfiguration())))
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

//    private ConfidentialLevel getLowestLevel(List<ConfidentialLevel> levels) {
//        int levelOrder = levels.stream().map(level -> {
//            if (ConfidentialLevel.NON_CONFIDENTIAL.equals(level))
//                return 0;
//            else if (ConfidentialLevel.LA.equals(level))
//                return 1;
//            else if (ConfidentialLevel.CTSC.equals(level))
//                return 2;
//            else
//                return -1;
//        }).sorted().findFirst().orElse(-1);
//
//        if (levelOrder == 0)
//            return ConfidentialLevel.NON_CONFIDENTIAL;
//        else if (levelOrder == 1)
//            return ConfidentialLevel.LA;
//        else if (levelOrder == 2)
//            return ConfidentialLevel.CTSC;
//        else
//            return null;
//    }
}
