package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.net.URLConnection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CafcassNotificationService {
    private static final String SUBJECT = "Court Ref. %s.- %s";
    private final EmailService emailService;
    private final DocumentDownloadService documentDownloadService;
    private final CafcassEmailConfiguration configuration;

    public void sendEmail(CaseData caseData,
                          Set<DocumentReference> documentReferences,
                          CafcassRequestEmailContentProvider provider,
                          String messageParam) {
        log.info("For case id {} notifying Cafcass for {}",
            caseData.getId(),
            messageParam);

        String subject = String.format(SUBJECT, caseData.getFamilyManCaseNumber(), provider.getType());

        emailService.sendEmail(configuration.getSender(),
            EmailData.builder()
                .recipient(provider.getRecipient().apply(configuration))
                .subject(subject)
                .attachments(getEmailAttachments(documentReferences))
                .message(String.format(provider.getContent(),
                    messageParam))
                .build());

        log.info("For case id {} notification sent to Cafcass for {}",
            caseData.getId(),
            messageParam);
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
