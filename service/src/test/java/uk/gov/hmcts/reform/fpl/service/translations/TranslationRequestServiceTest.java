package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.tranlsation.TranslationEmailConfiguration;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslationRequest;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;

class TranslationRequestServiceTest {

    private static final String SENDER_EMAIL = "senderEmail";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String ORIGINAL_DOCUMENT_BINARY_URL = "originalDocumentBinaryUrl";
    private static final byte[] ORIGINAL_DOCUMENT_CONTENT = "OriginalDocumentContent".getBytes();
    private static final String ORIGINAL_DOCUMENT_FILENAME = "banana.pdf";
    private static final LanguageTranslationRequirement LANGUAGE_TRANSLATION_REQUIREMENT =
        LanguageTranslationRequirement.WELSH_TO_ENGLISH;
    private static final DocmosisTranslationRequest DOCMOSIS_TRANSLATION_REQUEST = mock(
        DocmosisTranslationRequest.class);
    private static final byte[] REQUEST_FORM_CONTENT = "RequestFormContent".getBytes();
    private static final String MESSAGE = "message";
    private final TranslationEmailConfiguration configuration = mock(TranslationEmailConfiguration.class);
    private final EmailService emailService = mock(EmailService.class);
    private final TranslationRequestEmailContentProvider contentProvider =
        mock(TranslationRequestEmailContentProvider.class);
    private final TranslationRequestFormCreationService requestFormCreationService = mock(
        TranslationRequestFormCreationService.class);
    private final DocmosisTranslationRequestFactory translationRequestFactory =
        mock(DocmosisTranslationRequestFactory.class);
    private final DocumentDownloadService documentDownloadService = mock(DocumentDownloadService.class);

    private final TranslationRequestService underTest = new TranslationRequestService(
        configuration,
        emailService,
        contentProvider,
        requestFormCreationService,
        translationRequestFactory,
        documentDownloadService
    );

    @Test
    void testIfLanguageRequirementNo() {
        underTest.sendRequest(mock(CaseData.class),
            Optional.of(LanguageTranslationRequirement.NO), mock(DocumentReference.class));

        verifyNoIteractionWithServices();
    }

    @Test
    void testIfLanguageRequirementEmpty() {
        underTest.sendRequest(mock(CaseData.class),
            Optional.empty(), mock(DocumentReference.class));

        verifyNoIteractionWithServices();
    }

    @Test
    void testSendRequest() {
        CaseData caseData = CaseData.builder().build();

        when(configuration.getRecipient()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(ORIGINAL_DOCUMENT_BINARY_URL)).thenReturn(
            ORIGINAL_DOCUMENT_CONTENT);
        when(translationRequestFactory.create(caseData,
            LANGUAGE_TRANSLATION_REQUIREMENT,
            "",
            ORIGINAL_DOCUMENT_CONTENT))
            .thenReturn(DOCMOSIS_TRANSLATION_REQUEST);
        when(requestFormCreationService.buildTranslationRequestDocuments(DOCMOSIS_TRANSLATION_REQUEST)).thenReturn(
            DocmosisDocument.builder().bytes(REQUEST_FORM_CONTENT).build());
        when(contentProvider.generate(LANGUAGE_TRANSLATION_REQUIREMENT)).thenReturn(MESSAGE);

        underTest.sendRequest(caseData,
            Optional.of(LANGUAGE_TRANSLATION_REQUIREMENT),
            DocumentReference.builder().binaryUrl(ORIGINAL_DOCUMENT_BINARY_URL)
                .filename(ORIGINAL_DOCUMENT_FILENAME)
                .build()
        );

        verify(emailService).sendEmail(SENDER_EMAIL, EmailData.builder()
            .recipient(RECIPIENT_EMAIL)
            .subject("Translation request, FPL")
            .attachments(Set.of(
                document("application/pdf", ORIGINAL_DOCUMENT_CONTENT, ORIGINAL_DOCUMENT_FILENAME),
                document("application/msword", REQUEST_FORM_CONTENT, "translationRequestForm.doc")
            )).message(MESSAGE)
            .build());
    }

    private void verifyNoIteractionWithServices() {
        verifyNoInteractions(
            configuration,
            emailService,
            contentProvider,
            requestFormCreationService,
            translationRequestFactory,
            documentDownloadService
        );
    }

}
