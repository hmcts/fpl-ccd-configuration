package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.LargeFilesNotificationData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentMetadataDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.net.URLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.LARGE_ATTACHEMENTS;

@Service
@Slf4j
public class CafcassNotificationService {

    private final EmailService emailService;
    private final DocumentDownloadService documentDownloadService;
    private final CafcassEmailConfiguration configuration;

    private final CafcassLookupConfiguration lookupConfiguration;
    private final CaseUrlService caseUrlService;
    private final DocumentMetadataDownloadService documentMetadataDownloadService;
    private final long maxAttachmentSize;
    private static final long  MEGABYTE = 1024L * 1024L;

    @Autowired
    public CafcassNotificationService(EmailService emailService,
                                      DocumentDownloadService documentDownloadService,
                                      CafcassEmailConfiguration configuration,
                                      CafcassLookupConfiguration lookupConfiguration,
                                      CaseUrlService caseUrlService,
                                      DocumentMetadataDownloadService documentMetadataDownloadService,
                                      @Value("${cafcass.notification.maxMbAttachementSize:25}")
                                      long maxAttachmentSize) {
        this.emailService = emailService;
        this.documentDownloadService = documentDownloadService;
        this.configuration = configuration;
        this.lookupConfiguration = lookupConfiguration;
        this.caseUrlService = caseUrlService;
        this.documentMetadataDownloadService = documentMetadataDownloadService;
        this.maxAttachmentSize = maxAttachmentSize;
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
        log.info("For case id: {} notifying Cafcass for: {}",
            caseData.getId(),
            provider.name());

        final Map<String, DocumentReference> documentMetaData = documentReferences.stream()
                .map(DocumentReference::getUrl)
                .collect(toMap(identity(),
                    documentMetadataDownloadService::getDocumentMetadata,
                    (existing, replacement) -> existing));

        long totalDocSize = documentMetaData.values().stream()
                .mapToLong(doc -> defaultIfNull(doc.getSize(), 0L))
                .sum();

        if (totalDocSize / MEGABYTE  <= maxAttachmentSize) {
            sendAsAttachment(caseData, documentReferences,
                () -> provider.getRecipient().apply(configuration),
                () -> provider.getType().apply(caseData, cafcassData),
                cafcassData, provider.getContent(), provider.name());
        } else {
            evaluateAndSend(caseData, documentReferences, provider, cafcassData, totalDocSize, documentMetaData);
        }
    }

    public void sendEmail(CaseData caseData,
                          Set<DocumentReference> documentReferences,
                          CafcassEmailContentProvider provider,
                          CafcassData cafcassData) {
        log.info("For case id: {} notifying Cafcass for: {}",
            caseData.getId(),
            provider.name());

        final Map<String, DocumentReference> documentMetaData = documentReferences.stream()
            .map(DocumentReference::getUrl)
            .collect(toMap(identity(),
                documentMetadataDownloadService::getDocumentMetadata));

        long totalDocSize = documentMetaData.values().stream()
            .mapToLong(doc -> defaultIfNull(doc.getSize(), 0L))
            .sum();

        if (totalDocSize / MEGABYTE <= maxAttachmentSize) {
            sendAsAttachment(caseData, documentReferences,
                () -> provider.getRecipient().apply(lookupConfiguration, caseData),
                () -> provider.getType().apply(caseData, cafcassData),
                cafcassData, provider.getContent(), provider.name());
        } else {
            log.info("For case id {}, sum of file size {}",
                caseData.getId(),
                totalDocSize);
            LargeFilesNotificationData largeFileNotificationData = getLargFileNotificationData(
                caseData, documentReferences, caseUrlService);
            largeFileNotificationData.setOriginalCafcassData(cafcassData);

            emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                    .recipient(provider.getRecipient().apply(lookupConfiguration, caseData))
                    .subject(provider.getType().apply(caseData, cafcassData))
                    .message(provider.getLargeFileContent().apply(caseData, largeFileNotificationData))
                    .priority(cafcassData.isUrgent())
                    .build()
            );
            log.info("For case id {} large file uploaded notification sent to Cafcass for {}",
                caseData.getId(),
                provider.name());
        }
    }

    private void sendAsAttachment(final CaseData caseData,
                                  final Set<DocumentReference> documentReferences,
                                  final Supplier<String> recipientProvider,
                                  final Supplier<String> subjectProvider,
                                  final CafcassData cafcassData,
                                  final BiFunction<CaseData, CafcassData, String> content,
                                  final String providerName) {
        emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                        .recipient(recipientProvider.get())
                        .subject(subjectProvider.get())
                        .attachments(getEmailAttachments(documentReferences))
                        .message(content.apply(caseData, cafcassData))
                        .priority(cafcassData.isUrgent())
                        .build()
        );
        log.info("For case id {} notification sent to Cafcass for {}",
                caseData.getId(),
                providerName);
    }

    private void evaluateAndSend(final CaseData caseData,
                                 final Set<DocumentReference> documentReferences,
                                 final CafcassRequestEmailContentProvider provider,
                                 final CafcassData cafcassData,
                                 final long totalDocSize,
                                 final Map<String, DocumentReference> documentMetaData) {
        log.info("For case id {}, sum of file size is {} mb",
                caseData.getId(),
                totalDocSize / MEGABYTE);

        Set<DocumentReference> updatedDocReferences = documentReferences.stream()
                .map(documentReference -> {
                    DocumentReference documentRef = documentMetaData.get(documentReference.getUrl());
                    documentRef.setType(documentReference.getType());
                    return documentRef;
                }).collect(toSet());

        updatedDocReferences.stream()
                .map(DocumentReference::getUrl)
                .map(documentMetaData::get)
                .forEach(documentReference -> {
                    if (documentReference.getSize() / MEGABYTE <= maxAttachmentSize) {
                        String message = String.join(" : ",
                                "Document attached is",
                                documentReference.getFilename());
                        sendAsAttachment(caseData, Set.of(documentReference),
                            () -> provider.getRecipient().apply(configuration),
                            () -> provider.getType().apply(caseData, cafcassData),
                            cafcassData,
                            (caseDataObj, cafcassDataObj) -> message,
                            provider.name());
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

        emailService.sendEmail(configuration.getSender(),
            EmailData.builder()
                .recipient(LARGE_ATTACHEMENTS.getRecipient().apply(configuration))
                .subject(LARGE_ATTACHEMENTS.getType().apply(caseData, largeFileNotificationData))
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

    private LargeFilesNotificationData getLargFileNotificationData(CaseData caseData,
                                                                   Set<DocumentReference> documentReferences,
                                                                   CaseUrlService caseUrlService) {
        String fileNames = documentReferences.stream()
            .map(DocumentReference::getFilename)
            .collect(Collectors.joining(", "));
        return LargeFilesNotificationData.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .documentName(fileNames)
            .caseUrl(caseUrlService.getCaseUrl(caseData.getId()))
            .build();
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
