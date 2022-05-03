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
import java.util.Set;
import java.util.stream.Collectors;

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
    private final long maxAttachementSize;
    private static final long  MEGABYTE = 1024L * 1024L;

    @Autowired
    public CafcassNotificationService(EmailService emailService,
                                      DocumentDownloadService documentDownloadService,
                                      CafcassEmailConfiguration configuration,
                                      CafcassLookupConfiguration lookupConfiguration,
                                      CaseUrlService caseUrlService,
                                      DocumentMetadataDownloadService documentMetadataDownloadService,
                                      @Value("${cafcass.notification.maxMbAttachementSize:25}")
                                      long maxAttachementSize) {
        this.emailService = emailService;
        this.documentDownloadService = documentDownloadService;
        this.configuration = configuration;
        this.lookupConfiguration = lookupConfiguration;
        this.caseUrlService = caseUrlService;
        this.documentMetadataDownloadService = documentMetadataDownloadService;
        this.maxAttachementSize = maxAttachementSize;
    }

    public void sendEmail(CaseData caseData,
                          Set<DocumentReference> documentReferences,
                          CafcassRequestEmailContentProvider provider,
                          CafcassData cafcassData) {
        log.info("For case id: {} notifying Cafcass for: {}",
            caseData.getId(),
            provider.name());

        long totalDocSize = documentReferences.stream()
                .map(DocumentReference::getUrl)
                .map(documentMetadataDownloadService::getDocumentMetadata)
                .mapToLong((doc) -> defaultIfNull(doc.getSize(), 0L))
                .sum();

        if (totalDocSize / MEGABYTE  <= maxAttachementSize) {
            emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                    .recipient(provider.getRecipient().apply(configuration))
                    .subject(provider.getType().apply(caseData, cafcassData))
                    .attachments(getEmailAttachments(documentReferences))
                    .message(provider.getContent().apply(caseData, cafcassData))
                    .priority(cafcassData.isUrgent())
                    .build()
            );
            log.info("For case id {} notification sent to Cafcass for {}",
                    caseData.getId(),
                    provider.name());
        } else {
            log.info("For case id {}, sum of file size {}",
                    caseData.getId(),
                    totalDocSize);
            LargeFilesNotificationData largFileNotificationData = getLargFileNotificationData(
                    caseData, documentReferences, caseUrlService);

            emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                    .recipient(LARGE_ATTACHEMENTS.getRecipient().apply(configuration))
                    .subject(LARGE_ATTACHEMENTS.getType().apply(caseData, largFileNotificationData))
                    .message(LARGE_ATTACHEMENTS.getContent().apply(caseData, largFileNotificationData))
                    .priority(cafcassData.isUrgent())
                    .build()
            );
            log.info("For case id {} notification sent to Cafcass for {}",
                    caseData.getId(),
                    LARGE_ATTACHEMENTS.name());
        }
    }

    public void sendEmail(CaseData caseData,
                          Set<DocumentReference> documentReferences,
                          CafcassEmailContentProvider provider,
                          CafcassData cafcassData) {
        log.info("For case id: {} notifying Cafcass for: {}",
            caseData.getId(),
            provider.name());

        long totalDocSize = documentReferences.stream()
            .map(DocumentReference::getUrl)
            .map(documentMetadataDownloadService::getDocumentMetadata)
            .mapToLong((doc) -> defaultIfNull(doc.getSize(), 0L))
            .sum();
        long attachmentSize = totalDocSize / MEGABYTE;

        if (attachmentSize <= maxAttachementSize) {
            emailService.sendEmail(configuration.getSender(),
                EmailData.builder()
                    .recipient(provider.getRecipient().apply(lookupConfiguration, caseData))
                    .subject(provider.getType().apply(caseData, cafcassData))
                    .attachments(getEmailAttachments(documentReferences))
                    .message(provider.getContent().apply(caseData, cafcassData))
                    .priority(cafcassData.isUrgent())
                    .build()
            );
            log.info("For Case id {} notification sent to Cafcass (CafcassEmailContentProvider) for {}",
                caseData.getId(),
                provider.name());
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
