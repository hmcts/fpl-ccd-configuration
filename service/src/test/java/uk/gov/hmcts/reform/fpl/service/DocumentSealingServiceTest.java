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

import static org.assertj.core.api.Assertions.assertThat;
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
    private DocumentDownloadService documentDownloadService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @InjectMocks
    private DocumentSealingService documentSealingService;

    @Test
    void shouldSealDocument() throws Exception {
        byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        byte[] expectedSealedDocumentBinaries = readBytes("documents/document-sealed.pdf");
        final Document sealedDocument = testDocument();
        final DocumentReference inputDocumentReference = testDocumentReference();
        final DocumentReference sealedDocumentReference = buildFromDocument(sealedDocument);

        when(documentDownloadService.downloadDocument(inputDocumentReference.getBinaryUrl()))
            .thenReturn(inputDocumentBinaries);

        when(uploadDocumentService.uploadPDF(any(), any()))
            .thenReturn(sealedDocument);

        DocumentReference actualSealedDocumentReference = documentSealingService.sealDocument(inputDocumentReference);

        verify(uploadDocumentService)
            .uploadPDF(actualDocumentBinaries.capture(), eq(inputDocumentReference.getFilename()));

        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocumentReference);
        assertThat(actualDocumentBinaries.getValue()).isEqualTo(expectedSealedDocumentBinaries);
    }

}
