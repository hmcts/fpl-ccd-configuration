package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TranslatedDocumentGeneratorTest {

    private static final String BINARY_URL = "BINARY_URL";
    private static final byte[] UPLOADED_FILE_BYTES = "uploadedFileBytes".getBytes();
    private static final String FILENAME = "FILENAME";
    private static final byte[] CONVERTED_PDF_FILE_BYTES = "ConvertedPDFFile".getBytes();
    private static final byte[] CONVERTED_SEALED_PDF_FILE_BYTES = "ConvertedAndSealedPDFFile".getBytes();
    private static final Court COURT = Court.builder().build();
    private final DocumentSealingService documentSealingService = mock(DocumentSealingService.class);
    private final DocumentConversionService documentConversionService = mock(DocumentConversionService.class);
    private final DocumentDownloadService documentDownloadService = mock(DocumentDownloadService.class);


    private final TranslatedDocumentGenerator underTest = new TranslatedDocumentGenerator(
        documentSealingService,
        documentConversionService,
        documentDownloadService
    );

    @Test
    void testProcessor() {

        when(documentDownloadService.downloadDocument(BINARY_URL)).thenReturn(UPLOADED_FILE_BYTES);
        when(documentConversionService.convertToPdf(UPLOADED_FILE_BYTES,
            FILENAME)).thenReturn(CONVERTED_PDF_FILE_BYTES);
        when(documentSealingService.sealDocument(CONVERTED_PDF_FILE_BYTES, COURT, SealType.WELSH)).thenReturn(
            CONVERTED_SEALED_PDF_FILE_BYTES);

        byte[] actual = underTest.generate(CaseData.builder()
            .court(COURT)
            .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                .uploadTranslationsTranslatedDoc(DocumentReference.builder()
                    .binaryUrl(BINARY_URL)
                    .filename(FILENAME)
                    .build())
                .build())
            .build());

        assertThat(actual).isEqualTo(CONVERTED_SEALED_PDF_FILE_BYTES);
    }
}
