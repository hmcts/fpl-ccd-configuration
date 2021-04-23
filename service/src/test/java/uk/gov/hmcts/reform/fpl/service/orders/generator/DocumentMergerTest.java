package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DocumentMergerTest {
    @Mock
    private DocumentConversionService documentConversionService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @InjectMocks
    private DocumentMerger underTest;

    private static final String ORIGINAL_FILENAME = "document1.pdf";
    private static final String ADDITIONAL_FILENAME = "document2.pdf";

    private static final DocmosisDocument DOCMOSIS_DOCUMENT = DocmosisDocument.builder()
        .documentTitle(ORIGINAL_FILENAME).bytes(new byte[]{1}).build();

    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference(ADDITIONAL_FILENAME);

    @Test
    void shouldMergeTheDocuments() {
        final byte[] originalDocument = readBytes("documents/document1.pdf");
        final byte[] additionalDocument = readBytes("documents/document2.pdf");
        final byte[] additionalDocumentBytes = new byte[]{1, 2};
        final byte[] mergedDocument = readBytes("documents/merged.pdf");

        when(documentConversionService.convertToPdf(eq(DOCMOSIS_DOCUMENT.getBytes()), anyString()))
            .thenReturn(originalDocument);

        when(documentDownloadService.downloadDocument(eq(DOCUMENT_REFERENCE.getBinaryUrl())))
            .thenReturn(additionalDocumentBytes);
        when(documentConversionService.convertToPdf(eq(additionalDocumentBytes), anyString()))
            .thenReturn(additionalDocument);

        DocmosisDocument actualMergedPdf = underTest.mergeDocuments(DOCMOSIS_DOCUMENT, List.of(DOCUMENT_REFERENCE));

        assertThat(actualMergedPdf.getDocumentTitle()).isEqualTo(DOCMOSIS_DOCUMENT.getDocumentTitle());
        assertThat(actualMergedPdf.getBytes()).contains(mergedDocument);
    }

    @Test
    void shouldMergeTheDocumentsWhenAdditionalDocumentsAreEmpty() {
        DocmosisDocument actualMergedPdf = underTest.mergeDocuments(DOCMOSIS_DOCUMENT, List.of());

        assertThat(actualMergedPdf).isEqualTo(DOCMOSIS_DOCUMENT);
    }

    @Test
    void shouldThrowExceptionWhenAdditionalDocumentIsCorrupted() {
        final byte[] additionalDocumentBytes = new byte[]{1, 2};

        when(documentConversionService.convertToPdf(eq(DOCMOSIS_DOCUMENT.getBytes()), anyString()))
            .thenReturn(new byte[]{1});

        when(documentDownloadService.downloadDocument(eq(DOCUMENT_REFERENCE.getBinaryUrl())))
            .thenReturn(additionalDocumentBytes);
        when(documentConversionService.convertToPdf(eq(additionalDocumentBytes), anyString()))
            .thenReturn(additionalDocumentBytes);

        Exception exception = assertThrows(DocumentMergeException.class,
            () -> underTest.mergeDocuments(DOCMOSIS_DOCUMENT, List.of(DOCUMENT_REFERENCE))
        );

        assertThat(exception.getMessage()).isEqualTo(
            "Exception occurred while merging documents for " + DOCMOSIS_DOCUMENT.getDocumentTitle());
    }

}
