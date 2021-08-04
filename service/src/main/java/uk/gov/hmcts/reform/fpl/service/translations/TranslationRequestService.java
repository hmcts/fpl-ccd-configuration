package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.tranlsation.TranslationEmailConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;

import java.net.URLConnection;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TranslationRequestService {

    private static final String TRANSLATION_SUBJECT = "Translation request, FPL";
    private final TranslationEmailConfiguration configuration;
    private final EmailService emailService;
    private final TranslationRequestEmailContentProvider contentProvider;
    private final TranslationRequestFormCreationService translationRequestFormCreationService;
    private final DocmosisTranslationRequestFactory translationRequestFactory;
    private final DocumentDownloadService documentDownloadService;

    public void sendRequest(CaseData caseData, Optional<LanguageTranslationRequirement> languageOpt,
                            DocumentReference document) {

        log.info("Sending translation request with payload language '{}'", languageOpt);

        if (languageOpt.isEmpty() || languageOpt.get() == LanguageTranslationRequirement.NO) {
            return;
        }
        LanguageTranslationRequirement language = languageOpt.get();

        byte[] originalDocumentContent = documentDownloadService.downloadDocument(document.getBinaryUrl());

        EmailAttachment originalDocument = document(
            URLConnection.guessContentTypeFromName(document.getFilename()),
            originalDocumentContent,
            document.getFilename());

        EmailAttachment translationRequest = document(RenderFormat.WORD.getMediaType(),
            translationRequestFormCreationService.buildTranslationRequestDocuments(
                translationRequestFactory.create(caseData, language, "", originalDocumentContent)).getBytes(),
            "translationRequestForm.doc"
        );

        emailService.sendEmail(configuration.getSender(), EmailData.builder()
            .recipient(configuration.getRecipient())
            .subject(TRANSLATION_SUBJECT)
            .attachments(Set.of(
                translationRequest,
                originalDocument
            )).message(contentProvider.generate(language))
            .build());
    }

}
