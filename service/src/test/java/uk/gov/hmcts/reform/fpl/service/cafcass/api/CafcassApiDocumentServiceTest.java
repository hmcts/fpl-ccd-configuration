package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CafcassApiDocumentServiceTest {
    private SecureDocStoreService secureDocStoreService = mock(SecureDocStoreService.class);
    private UploadDocumentService uploadDocumentService = mock(UploadDocumentService.class);
    private CoreCaseDataService coreCaseDataService = mock(CoreCaseDataService.class);
    private CafcassApiDocumentService underTest;

    @BeforeEach
    void setUpWithMockConverters() {
        underTest = new CafcassApiDocumentService(secureDocStoreService,
            uploadDocumentService,
            coreCaseDataService);
    }

    @Test
    void shouldReturnDocumentBinary() {
        UUID docId = UUID.randomUUID();
        byte[] docBinary = "This is a document".getBytes();
        when(secureDocStoreService.downloadDocument(docId.toString())).thenReturn(docBinary);

        assertArrayEquals(docBinary, underTest.downloadDocumentByDocumentId(docId.toString()));
    }

    @Test
    void shouldReturnTrueIfValid() throws IOException {
        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.APPLICATION_PDF_VALUE, fileBytes);

        assertEquals(underTest.isValidFile(file), true);
    }

    @Test
    void shouldReturnFalseIfFileNotPdf() throws IOException {
        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        assertEquals(underTest.isValidFile(file), false);
    }

    @Test
    void shouldReturnFalseIfFileEmpty() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, "".getBytes());

        assertEquals(underTest.isValidFile(file), false);
    }
}
