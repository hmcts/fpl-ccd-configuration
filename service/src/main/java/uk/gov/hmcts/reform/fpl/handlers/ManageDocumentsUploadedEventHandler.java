package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.time.LocalDate;
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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType.CORRESPONDENCE_UPLOADED;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.SKELETON_ARGUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService.PDF;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsUploadedEventHandler {

    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassNotificationService cafcassNotificationService;
    private final FurtherEvidenceNotificationService furtherEvidenceNotificationService;
    private final SendDocumentService sendDocumentService;
    private final TranslationRequestService translationRequestService;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final UserService userService;

    private static final String BULLET_POINT = "•";
    public static final String FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION = "Further documents for main application";
    public static final String CORRESPONDENCE = "Correspondence";

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
                            .values().stream()
                            .flatMap(List::stream)
                            .toList();

                    if (!documentsToBeSent.isEmpty()) {
                        List<String> newDocumentNames = unwrapElements(documentsToBeSent).stream()
                            .map(NotifyDocumentUploaded::getNameForNotification)
                            .toList();

                        if (!newDocumentNames.isEmpty()) {
                            furtherEvidenceNotificationService.sendNotification(event.getCaseData(), recipients,
                                event.getInitiatedBy().getFullName(), newDocumentNames);
                        }
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
                    .toList();

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
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!documentsToBeSent.isEmpty()) {
                Map<CafcassRequestEmailContentProvider, List<DocumentType>> emailTemplateMap =
                    documentsToBeSent.keySet().stream()
                        .collect(groupingBy(docType ->
                                docType.getNotificationConfiguration().getCafcassRequestEmailContentProvider(),
                                toList()));

                emailTemplateMap.forEach((cafcassEmailContentProvider, documentTypeList) -> {
                    String documentTypes = documentTypeList.stream()
                        .map(docType ->
                            String.join(" ", BULLET_POINT,
                                docType.getDescription().replace("└─ ", "")))
                        .collect(joining("\n"));

                    String subjectInfo = documentTypeList.stream()
                        .map(documentType -> (COURT_CORRESPONDENCE.equals(documentType))
                            ? CORRESPONDENCE : FURTHER_DOCUMENTS_FOR_MAIN_APPLICATION)
                        .findFirst().orElse("UNKNOWN");

                    cafcassNotificationService.sendEmail(
                        caseData,
                        documentTypeList.stream()
                            .map(docType ->
                                unwrapElements(documentsToBeSent.get(docType)).stream()
                                    .map(NotifyDocumentUploaded::getDocument)
                                    .map(documentReference -> documentReference.toBuilder()
                                        .type(docType.toString().replace("_", " "))
                                        .build())
                                    .collect(toSet()))
                            .flatMap(Set::stream)
                            .collect(toSet()),
                        cafcassEmailContentProvider,
                        NewDocumentData.builder()
                            .documentTypes(documentTypes)
                            .emailSubjectInfo(subjectInfo)
                            .build());
                });
            }
        } else {
            log.info("Not notifying Cafcass england");
        }
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(final ManageDocumentsUploadedEvent event) {
        CaseData caseData = event.getCaseData();
        if (YesNo.YES.equals(YesNo.fromString(caseData.getLanguageRequirement()))) {
            consolidateMapByConfiguration(event, DocumentUploadedNotificationConfiguration::getSendToTranslationTeam)
                .forEach((documentType, documents) ->
                    unwrapElements(documents).forEach(document ->
                        translationRequestService.sendRequest(caseData,
                            Optional.ofNullable(document.getTranslationRequirements()),
                            document.getDocument(),
                            String.format("%s - %s - %s", Optional.ofNullable(documentType.getDescription()),
                                document.getNameForNotification(),
                                formatLocalDateBaseUsingFormat(LocalDate.now(), DATE)))));
        }
    }

    private Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>>
            buildConfigurationMapGroupedByRecipient(final ManageDocumentsUploadedEvent event) {

        final CaseData caseData = event.getCaseData();

        Map<Set<String>, Function<DocumentUploadedNotificationConfiguration, ConfidentialLevel>> resultMap =
            new HashMap<>();

        // designated LA
        Set<String> designatedLA = furtherEvidenceNotificationService
            .getDesignatedLocalAuthorityRecipientsOnly(caseData);
        if (designatedLA.isEmpty()) {
            log.info("No recipient found for designated LA");
        } else {
            resultMap.put(designatedLA,
                DocumentUploadedNotificationConfiguration::getSendToDesignatedLA);
        }

        // secondary LA
        Set<String> secondaryLA = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData);
        if (secondaryLA.isEmpty()) {
            log.info("No recipient found for secondary LA");
        } else {
            resultMap.put(secondaryLA,
                DocumentUploadedNotificationConfiguration::getSendToSecondaryLA);
        }

        // legal representative
        Set<String> legalRepresentative = furtherEvidenceNotificationService.getLegalRepresentativeOnly(caseData);
        if (legalRepresentative.isEmpty()) {
            log.info("No recipient found for legal representative");
        } else {
            resultMap.put(legalRepresentative,
                DocumentUploadedNotificationConfiguration::getSendToLegalRepresentative);
        }

        // fall back inbox
        if (designatedLA.isEmpty() && secondaryLA.isEmpty() && legalRepresentative.isEmpty()) {
            log.info("Add fall back inbox to recipient list");
            Set<String> fallbackInbox = furtherEvidenceNotificationService.getFallbackInbox();
            resultMap.put(fallbackInbox, (config) -> ConfidentialLevel.CTSC);
        }

        // cafcass representative
        Set<String> cafcassRepresentative = furtherEvidenceNotificationService.getCafcassRepresentativeEmails(caseData);
        if (cafcassRepresentative.isEmpty()) {
            log.info("No recipient found for cafcass representative");
        } else {
            resultMap.put(cafcassRepresentative,
                DocumentUploadedNotificationConfiguration::getSendToCafcassRepresentative);
        }

        // respondent solicitor
        Set<String> respondentSolicitor = furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData);
        if (respondentSolicitor.isEmpty()) {
            log.info("No recipient found for respondent solicitor");
        } else {
            resultMap.put(respondentSolicitor,
                DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor);
        }

        // child solicitor
        Set<String> childSolicitor = furtherEvidenceNotificationService.getChildSolicitorEmails(caseData);
        if (childSolicitor.isEmpty()) {
            log.info("No recipient found for child solicitor");
        } else {
            resultMap.put(childSolicitor,
                DocumentUploadedNotificationConfiguration::getSendToChildSolicitor);
        }

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            Optional<String> recipientIsWelsh =
                cafcassLookupConfiguration.getCafcassWelsh(caseData.getCaseLocalAuthority())
                    .map(CafcassLookupConfiguration.Cafcass::getEmail);
            if (recipientIsWelsh.isPresent()) {
                resultMap.put(Set.of(recipientIsWelsh.get()),
                    DocumentUploadedNotificationConfiguration::getSendToCafcassWelsh);
            } else {
                log.info("No recipient found for Cafcass Welsh");
            }
        } else {
            log.info("Not notifying Cafcass Welsh");
        }

        return resultMap;
    }

    private boolean isHearingDocument(DocumentType documentType) {
        return COURT_BUNDLE.equals(documentType) || CASE_SUMMARY.equals(documentType)
               || POSITION_STATEMENTS.equals(documentType) || SKELETON_ARGUMENTS.equals(documentType);
    }

    /**
     * Base on the notification configuration of each file type, returns all files uploaded in the event which satisfy
     * the confidential level configured.
     * e.g.
     * Given: A user has CTSC level over DocumentType A and non-confidential level over DocumentType B.
     * If any, this method will return all newly uploaded documents of DocumentType A and only non-confidential
     * documents of DocumentType B.
     *
     * @param event the ManageDocumentsUploadedEvent
     * @param getConfidentialLevelFunction A function accepts the notification configuration of a document type and
     *                                     returns the confidential level of the targeted user type of that document
     *                                     type.
     *                                     e.g. To get the confidential level of respondent solicitor
     *                                         DocumentUploadedNotificationConfiguration::getSendToRespondentSolicitor
     * @return Lists of documents grouped by document type
     */
    private Map<DocumentType, List<Element<NotifyDocumentUploaded>>>
            consolidateMapByConfiguration(ManageDocumentsUploadedEvent event,
                                          Function<DocumentUploadedNotificationConfiguration,
                                              ConfidentialLevel> getConfidentialLevelFunction) {

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> nonConfidentialDocuments =
            event.getNewDocuments().entrySet().stream()
                .filter(entry -> entry.getKey().getNotificationConfiguration() != null)
                .filter(entry ->
                    getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration()) != null)
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> laLevelDocuments =
            event.getNewDocumentsLA().entrySet().stream()
                .filter(entry -> entry.getKey().getNotificationConfiguration() != null)
                .filter(entry -> {
                    ConfidentialLevel levelConfiguration =
                        getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration());
                    return ConfidentialLevel.LA.equals(levelConfiguration)
                           || ConfidentialLevel.CTSC.equals(levelConfiguration);
                })
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> ctscLevelDocuments =
            event.getNewDocumentsCTSC().entrySet().stream()
                .filter(entry -> entry.getKey().getNotificationConfiguration() != null)
                .filter(entry -> ConfidentialLevel.CTSC
                    .equals(getConfidentialLevelFunction.apply(entry.getKey().getNotificationConfiguration())))
                .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));

        return Stream.of(nonConfidentialDocuments, laLevelDocuments, ctscLevelDocuments)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(groupingBy(Map.Entry::getKey, flatMapping(entry -> entry.getValue().stream(), toList())));
    }

    @EventListener
    public void createWorkAllocationTask(ManageDocumentsUploadedEvent event) {
        CaseData caseData = event.getCaseData();

        if (!userService.isHmctsAdminUser()
            && (isNotEmpty(event.getNewDocuments().get(COURT_CORRESPONDENCE))
                || isNotEmpty(event.getNewDocumentsLA().get(COURT_CORRESPONDENCE))
                || isNotEmpty(event.getNewDocumentsCTSC().get(COURT_CORRESPONDENCE)))) {
            workAllocationTaskService.createWorkAllocationTask(caseData, CORRESPONDENCE_UPLOADED);
        }
    }
}
