package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.exceptions.EncryptedPdfUploadedException;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.io.UncheckedIOException;
import java.util.Map;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.HIGH_COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
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

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    @Mock
    private CourtService courtService;

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
        final Court court = Court.builder().build();
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);
        when(courtService.isHighCourtCase(court)).thenReturn(false);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, court, SealType.ENGLISH);

        verify(uploadDocumentService).uploadPDF(actualDocumentBinaries.capture(), eq(newFileName));
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
        assertThat(actualDocumentBinaries.getValue()).isEqualTo(expectedSealedDocumentBinaries);
    }

    @Test
    void shouldHighCourtSealAndUploadDocumentWithFileConversion() {
        final String fileName = "test.doc";
        final String newFileName = "test.pdf";
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final Court court = Court.builder().build();
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);
        Map<String, Object> docmosisData = Map.of("dateOfIssue", formatLocalDateToString(now(), DATE));
        DocmosisDocument docmosisDocument = testDocmosisDocument(DOCUMENT_CONTENT);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);
        when(courtService.isHighCourtCase(court)).thenReturn(true);
        when(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, HIGH_COURT_SEAL))
                .thenReturn(docmosisDocument);
        when(documentConversionService.convertToPdf(docmosisDocument.getBytes(), docmosisDocument.getDocumentTitle()))
                .thenReturn(inputDocumentBinaries);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, court, SealType.ENGLISH);

        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(docmosisData, HIGH_COURT_SEAL);
        verify(documentConversionService).convertToPdf(docmosisDocument.getBytes(),
                docmosisDocument.getDocumentTitle());
        verify(uploadDocumentService).uploadPDF(actualDocumentBinaries.capture(), eq(newFileName));
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
    }

    @Test
    void shouldHighCourtSealWithoutDateAndUploadDocumentWithFileConversion() {
        final String fileName = "test.doc";
        final String newFileName = "test.pdf";
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final Court court = Court.builder().build();
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);
        Map<String, Object> docmosisData = Map.of("dateOfIssue", formatLocalDateToString(now(), DATE));
        DocmosisDocument docmosisDocument = testDocmosisDocument(DOCUMENT_CONTENT);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);
        when(courtService.isHighCourtCase(court)).thenReturn(true);
        when(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, HIGH_COURT_SEAL))
                .thenReturn(docmosisDocument);
        when(documentConversionService.convertToPdf(docmosisDocument.getBytes(), docmosisDocument.getDocumentTitle()))
                .thenReturn(DOCUMENT_CONTENT);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, court, SealType.ENGLISH);

        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(docmosisData, HIGH_COURT_SEAL);
        verify(documentConversionService).convertToPdf(docmosisDocument.getBytes(),
                docmosisDocument.getDocumentTitle());
        verify(uploadDocumentService).uploadPDF(actualDocumentBinaries.capture(), eq(newFileName));
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
    }

    @Test
    void shouldSealAndUploadDocumentWithoutFileConversion() {
        final String fileName = "test.pdf";
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] expectedSealedDocumentBinaries = readBytes("documents/document-sealed.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);
        final Court court = Court.builder().build();

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);
        when(uploadDocumentService.uploadPDF(any(), any())).thenReturn(sealedDocument);

        final DocumentReference actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocumentReference, court, SealType.ENGLISH);

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
        final Court court = Court.builder().build();

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(notPdf);

        assertThrows(UncheckedIOException.class, () ->
            documentSealingService.sealDocument(inputDocumentReference, court, SealType.ENGLISH));
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsKeyEncryptedPdf() {
        final String fileName = "test.pdf";

        final byte[] inputDocumentBinaries = readBytes("documents/document-secured_256bitaes.pdf");
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final Court court = Court.builder().build();

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);

        assertThrows(EncryptedPdfUploadedException.class, () ->
            documentSealingService.sealDocument(inputDocumentReference, court, SealType.ENGLISH));
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsPasswordProtectedPdf() {
        final String fileName = "test.pdf";

        final byte[] inputDocumentBinaries = readBytes("documents/document-password-protected.pdf");
        final DocumentReference inputDocumentReference = testDocumentReference(fileName);
        final Court court = Court.builder().build();

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);
        when(documentConversionService.convertToPdf(inputDocumentBinaries, fileName)).thenReturn(inputDocumentBinaries);

        assertThrows(EncryptedPdfUploadedException.class, () ->
            documentSealingService.sealDocument(inputDocumentReference, court, SealType.ENGLISH));
    }
}
