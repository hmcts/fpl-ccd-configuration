package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CASE_DATA_WITH_ALL_ORDERS;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CONVERTED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.RENDERED_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.SEALED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.TEST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.TRANSLATED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UPLOADED_TRANSFORMED_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_3;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_4;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.dlElement;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(UploadTranslationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsControllerAboutToSubmitTest extends AbstractCallbackTest {


    UploadTranslationsControllerAboutToSubmitTest() {
        super("upload-translations");
    }

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocumentSealingService documentSealingService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldFinaliseDocumentsAboutToSubmit() {
        Court court = Court.builder().build();
        when(documentDownloadService.downloadDocument(TEST_DOCUMENT.getBinaryUrl())).thenReturn(TRANSLATED_DOC_BYTES);
        when(documentConversionService.convertToPdf(TRANSLATED_DOC_BYTES, TEST_DOCUMENT.getFilename())).thenReturn(
            CONVERTED_DOC_BYTES);
        when(documentSealingService.sealDocument(CONVERTED_DOC_BYTES, court, SealType.BILINGUAL))
            .thenReturn(SEALED_DOC_BYTES);
        when(uploadDocumentService.uploadDocument(SEALED_DOC_BYTES,
            "noticeo_c6-Welsh.pdf",
            RenderFormat.PDF.getMediaType()))
            .thenReturn(UPLOADED_TRANSFORMED_DOCUMENT);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            CASE_DATA_WITH_ALL_ORDERS.toBuilder()
                .court(court)
                .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                    .uploadTranslationsRelatedToDocument(RENDERED_DYNAMIC_LIST.toBuilder()
                        .value(dlElement(UUID_3, "Notice of proceedings (C6)"))
                        .build())
                    .uploadTranslationsOriginalDoc(DocumentReference.builder()
                        .filename("noticeo_c6.pdf")
                        .build())
                    .uploadTranslationsTranslatedDoc(TEST_DOCUMENT)
                    .build())
                .build()
        );

        CaseData updatedCaseData = extractCaseData(response);

        assertThat(updatedCaseData).isEqualTo(CASE_DATA_WITH_ALL_ORDERS.toBuilder()
            .court(court)
            .noticeOfProceedingsBundle(List.of(element(UUID_3, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .translatedDocument(DocumentReference.buildFromDocument(UPLOADED_TRANSFORMED_DOCUMENT))
                .translationRequirements(ENGLISH_TO_WELSH)
                .translationUploadDateTime(now())
                .build()
            ), element(UUID_4, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6a.pdf")
                    .build())
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()
            )))
            .caseProgressionReportEventData(CaseProgressionReportEventData.builder().build())
            .build());

    }

}
