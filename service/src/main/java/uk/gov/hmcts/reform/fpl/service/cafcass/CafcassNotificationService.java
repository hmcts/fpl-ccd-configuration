package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.LargeFilesNotificationData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentMetadataDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Set.of;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.LARGE_ATTACHEMENTS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_CHILD;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@Slf4j
public class CafcassNotificationService {

    private final EmailService emailService;
    private final DocumentDownloadService documentDownloadService;
    private final CafcassEmailConfiguration configuration;
    private final CaseUrlService caseUrlService;
    private final DocumentMetadataDownloadService documentMetadataDownloadService;
    private final long maxAttachmentSize;
    private final FeatureToggleService featureToggleService;
    private static final long  MEGABYTE = 1024L * 1024L;
    private static final String SUBJECT_DELIMITER = "|";
    private static final String VALUE_TO_REPLACE = String.join("",SUBJECT_DELIMITER,"null");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public CafcassNotificationService(EmailService emailService,
                                      DocumentDownloadService documentDownloadService,
                                      CafcassEmailConfiguration configuration,
                                      CaseUrlService caseUrlService,
                                      DocumentMetadataDownloadService documentMetadataDownloadService,
                                      @Value("${cafcass.notification.maxMbAttachementSize:25}")
                                      long maxAttachmentSize,
                                      FeatureToggleService featureToggleService) {
        this.emailService = emailService;
        this.documentDownloadService = documentDownloadService;
        this.configuration = configuration;
        this.caseUrlService = caseUrlService;
        this.documentMetadataDownloadService = documentMetadataDownloadService;
        this.maxAttachmentSize = maxAttachmentSize;
        this.featureToggleService = featureToggleService;
    }

    public void sendEmail(CaseData caseData,
                          CafcassRequestEmailContentProvider provider,
                          CafcassData cafcassData) {
        sendEmail(caseData, emptySet(), provider, cafcassData);
    }

    public void sendEmail(CaseData caseData,
                          Set<DocumentReference> documentReferences,
                          CafcassRequestEmailContentProvider provider,
                          CafcassData cafcassData) {
        if (featureToggleService.isCafcassAPIEnabled(caseData.getCourt())) {
            log.info("For case id: {} Cafcass API is enabled, skip notifying Cafcass for: {} via SendGrid",
                caseData.getId(),
                provider.name());
            return;
        }

        log.info("For case id: {} notifying Cafcass for: {}",
            caseData.getId(),
            provider.name());

        final Map<String, DocumentReference> documentMetaData = documentReferences.stream()
                .collect(toMap(DocumentReference::getUrl,
                    docRef -> {
                        DocumentReference downloadedDocRef = documentMetadataDownloadService.getDocumentMetadata(
                                docRef.getUrl());
                        downloadedDocRef.setType(docRef.getType());
                        return downloadedDocRef;
                    },
                    (existing, replacement) -> existing));

        long totalDocSize = documentMetaData.values().stream()
                .mapToLong(DocumentReference::getSize)
                .sum();

        if (totalDocSize / MEGABYTE  <= maxAttachmentSize) {
            log.info("For case id {}, sum of file size is {} mb. Number of files: {}",
                caseData.getId(),
                totalDocSize / MEGABYTE,
                documentMetaData.values().size());
            if (featureToggleService.isCafcassSubjectCategorised()) {
                sendAsAttachment(caseData, Set.copyOf(documentMetaData.values()), provider, cafcassData,
                        provider.getContent());
            } else {
                sendAsMultipleAttachment(caseData, Set.copyOf(documentMetaData.values()), provider, cafcassData,
                        provider.getContent());
            }
        } else {
            evaluateAndSend(caseData, provider, cafcassData, totalDocSize, documentMetaData);
        }
    }


    private void sendAsMultipleAttachment(final CaseData caseData,
                                  final Set<DocumentReference> documentReferences,
                                  final CafcassRequestEmailContentProvider provider,
                                  final CafcassData cafcassData,
                                  final BiFunction<CaseData, CafcassData, String> content) {
        String subject = provider.getType().apply(caseData, cafcassData);
        log.info("For case id: {} notification subject:{}",
                caseData.getId(),
                subject);
        emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                        .recipient(provider.getRecipient().apply(configuration))
                        .subject(subject)
                        .attachments(getEmailAttachments(documentReferences))
                        .message(content.apply(caseData, cafcassData))
                        .build()
        );
        log.info("For case id {} notification sent to Cafcass for {} with multiple docs",
                caseData.getId(),
                provider.name());
    }

    private void sendAsAttachment(final CaseData caseData,
                                  final Set<DocumentReference> documentReferences,
                                  final CafcassRequestEmailContentProvider provider,
                                  final CafcassData cafcassData,
                                  final BiFunction<CaseData, CafcassData, String> content) {
        if (documentReferences.isEmpty()) {
            sendAsAttachment(
                    caseData,
                    Optional.empty(),
                    provider,
                    cafcassData,
                    content
            );
        }
        documentReferences
                .forEach(documentReference -> sendAsAttachment(
                        caseData,
                        Optional.of(documentReference),
                        provider,
                        cafcassData,
                        content
                ));
    }

    private void sendAsAttachment(final CaseData caseData,
                              final Optional<DocumentReference> documentReference,
                              final CafcassRequestEmailContentProvider provider,
                              final CafcassData cafcassData,
                              final BiFunction<CaseData, CafcassData, String> content) {
        String subject = getSubject(caseData, provider, cafcassData, documentReference);
        log.info("Subject: {} for doc reference type: {} ", subject,
                documentReference
                    .map(DocumentReference::getType)
                    .orElse(String.join(":", "not set for",provider.getLabel()))
        );

        Set<EmailAttachment> emailAttachments = getEmailAttachment(documentReference)
                .map(Set::of).orElse(emptySet());

        log.info("data in the document {} with total size: {} mb", emailAttachments,
            documentReference.stream().mapToLong(DocumentReference::getSize).sum() / MEGABYTE);

        emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                        .recipient(provider.getRecipient().apply(configuration))
                        .subject(subject)
                        .attachments(emailAttachments)
                        .message(content.apply(caseData, cafcassData))
                        .build()
        );
        log.info("For case id {} notification sent to Cafcass for {}",
                caseData.getId(),
                provider.name());
    }

    private String getSubject(final CaseData caseData,
                              final CafcassRequestEmailContentProvider provider,
                              final CafcassData cafcassData,
                              Optional<DocumentReference> docReference) {
        if (featureToggleService.isCafcassSubjectCategorised()
                && provider.isGenericSubject()
                && docReference.isPresent()) {
            DocumentReference documentReference = docReference.get();
            String additionalInfo = getAdditionalInfo(provider, cafcassData, documentReference);

            String lookupKey = Optional.ofNullable(
                            CaseUtils.toCamelCase(documentReference.getType(), false, ' ')
                    )
                    .map(key -> key.contains("'")
                            ? key.replace("'", "") : key)
                    .orElse("other");


            String cafcassDocumentMappingType = configuration.getDocumentType().get(lookupKey);

            String oldestChildsLastName = unwrapElements(caseData.getAllChildren()).stream()
                    .map(Child::getParty)
                    .filter(child -> Optional.ofNullable(child.getDateOfBirth()).isPresent())
                    .min(comparing(ChildParty::getDateOfBirth))
                    .map(ChildParty::getLastName)
                    .orElse("");

            String subject = String.join(SUBJECT_DELIMITER,
                    oldestChildsLastName,
                    caseData.getFamilyManCaseNumber(),
                    String.valueOf(caseData.getId()),
                    cafcassDocumentMappingType,
                    additionalInfo);

            return subject.replace(VALUE_TO_REPLACE, "");
        } else {
            return provider.getType().apply(caseData, cafcassData);
        }
    }

    private String getAdditionalInfo(CafcassRequestEmailContentProvider provider,
                                     CafcassData cafcassData,
                                     DocumentReference documentReference) {
        String additionalInfo = null;
        if (provider == ORDER) {
            String hearingDate = Optional.ofNullable(cafcassData.getHearingDate())
                    .map(localDateTime -> localDateTime.format(DATE_TIME_FORMATTER))
                    .orElse(null);

            additionalInfo = String.join(SUBJECT_DELIMITER,
                    hearingDate,
                    Optional.ofNullable(cafcassData.getOrderApprovalDate())
                    .map(localDateTime -> localDateTime.format(DATE_FORMATTER))
                    .orElse(LocalDate.now().format(DATE_FORMATTER))
            );

            documentReference.setType(ORDER.getLabel());
        } else if (provider == NOTICE_OF_HEARING) {
            additionalInfo = Optional.ofNullable(cafcassData.getHearingDate())
                    .map(localDateTime -> localDateTime.format(DATE_TIME_FORMATTER))
                    .orElse("NotSet");
            documentReference.setType(NOTICE_OF_HEARING.getLabel());
        } else if (provider == COURT_BUNDLE || provider == CASE_SUMMARY || provider == POSITION_STATEMENT_CHILD
                   || provider == POSITION_STATEMENT_RESPONDENT) {
            documentReference.setType(provider.getLabel());
        }

        return additionalInfo;
    }

    private void evaluateAndSend(final CaseData caseData,
                                 final CafcassRequestEmailContentProvider provider,
                                 final CafcassData cafcassData,
                                 final long totalDocSize,
                                 final Map<String, DocumentReference> documentMetaData) {
        log.info("For case id {}, sum of file size is {} mb",
                caseData.getId(),
                totalDocSize / MEGABYTE);

        documentMetaData.values()
                .forEach(documentReference -> {
                    if (documentReference.getSize() / MEGABYTE <= maxAttachmentSize) {
                        String message = String.join(" : ",
                                "Document attached is",
                                documentReference.getFilename());
                        sendAsAttachment(caseData, of(documentReference), provider, cafcassData,
                            (caseDataObj, cafcassDataObj) -> message);
                    } else {
                        sendAsLink(caseData, documentReference,
                                Optional.ofNullable(documentReference.getType())
                                        .orElse(provider.getLabel()));
                    }
                });
    }

    private void sendAsLink(final CaseData caseData,
                            final DocumentReference documentReferences,
                            final String notificationType) {


        LargeFilesNotificationData largeFileNotificationData = getLargeFileNotificationData(
                caseData, documentReferences, caseUrlService, notificationType);

        String subject = LARGE_ATTACHEMENTS.getType().apply(caseData, largeFileNotificationData);
        log.info("For case id: {} notification subject:{}",
                caseData.getId(),
                subject);

        emailService.sendEmail(configuration.getSender(),
            EmailData.builder()
                .recipient(LARGE_ATTACHEMENTS.getRecipient().apply(configuration))
                .subject(subject)
                .message(LARGE_ATTACHEMENTS.getContent().apply(caseData, largeFileNotificationData))
                .build()
        );

        log.info("For case id {} notification sent to Cafcass for {} and notification type {}",
                caseData.getId(),
                LARGE_ATTACHEMENTS.name(),
                notificationType);
    }

    private LargeFilesNotificationData getLargeFileNotificationData(CaseData caseData,
                                                                    DocumentReference documentReference,
                                                                    CaseUrlService caseUrlService,
                                                                    String notificationType) {
        return LargeFilesNotificationData.builder()
                .familyManCaseNumber(caseData.getFamilyManCaseNumber())
                .documentName(documentReference.getFilename())
                .caseUrl(caseUrlService.getCaseUrl(caseData.getId()))
                .notificationType(notificationType)
                .build();
    }

    private Optional<EmailAttachment> getEmailAttachment(Optional<DocumentReference> docReference) {
        return docReference.map(documentReference -> {
            byte[] documentContent = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());

            return document(
                    defaultIfNull(URLConnection.guessContentTypeFromName(documentReference.getFilename()),
                            "application/octet-stream"),
                    documentContent,
                    documentReference.getFilename());
        });
    }

    private Set<EmailAttachment> getEmailAttachments(Set<DocumentReference> documentReferences) {
        return documentReferences.stream()
            .map(documentReference -> {
                byte[] documentContent = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());

                return document(
                        defaultIfNull(URLConnection.guessContentTypeFromName(documentReference.getFilename()),
                                "application/octet-stream"),
                        documentContent,
                        documentReference.getFilename());
            })
            .collect(toSet());
    }
}
