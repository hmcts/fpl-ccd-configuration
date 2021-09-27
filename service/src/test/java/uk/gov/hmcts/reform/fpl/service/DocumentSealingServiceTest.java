package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DocumentSealingServiceTest {

    @Captor
    private ArgumentCaptor<byte[]> actualDocumentBinaries;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Mock
    private DocumentConversionService documentConversionService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @InjectMocks
    private DocumentSealingService documentSealingService;

    @Test
    void shouldSealAndUploadDocumentWithFileConversion() {
        final String fileName = "test.doc";
        final String newFileName = "test.pdf";
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] expectedSealedDocumentBinaries = readBytes("documents/document-sealed.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, SealType.ENGLISH);

        verify(uploadDocumentService).uploadPDF(actualDocumentBinaries.capture(), eq(newFileName));
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
        assertThat(actualDocumentBinaries.getValue()).isEqualTo(expectedSealedDocumentBinaries);
    }

    @Test
    void shouldSealAndUploadDocumentWithoutFileConversion() {
        final String fileName = "test.pdf";
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] expectedSealedDocumentBinaries = readBytes("documents/document-sealed.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, SealType.ENGLISH);

        verify(uploadDocumentService)
            .uploadPDF(actualDocumentBinaries.capture(), eq(inputDocumentReference.getFilename()));
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
        assertThat(actualDocumentBinaries.getValue()).isEqualTo(expectedSealedDocumentBinaries);
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsNotPdf() {
        final String fileName = "test.pdf";
        final byte[] notPdf = new byte[]{1};
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(notPdf);

        assertThrows(UncheckedIOException.class, () ->
            documentSealingService.sealDocument(inputDocumentReference, SealType.ENGLISH));
    }
}
