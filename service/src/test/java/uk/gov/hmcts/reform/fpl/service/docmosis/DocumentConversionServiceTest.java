package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class DocumentConversionServiceTest {

    private static final String BASE_URL = "baseUrl";
    private static final String ACCESS_KEY = "accessKey";
    private static final String DOCX_FILE_NAME = "cmo.docx";
    private static final String PDF_FILE_NAME = "cmo.pdf";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final DocmosisConfiguration configuration = mock(DocmosisConfiguration.class);
    private final DocumentDownloadService downloadService = mock(DocumentDownloadService.class);
    private final UploadDocumentService uploadService = mock(UploadDocumentService.class);

    private final DocumentConversionService underTest = new DocumentConversionService(
        restTemplate, configuration, downloadService, uploadService
    );

    @Test
    void shouldReturnSameDocumentReferenceIfItIsPdf() {
        final DocumentReference inputDocumentReference = DocumentReference.builder()
            .filename(PDF_FILE_NAME)
            .build();

        DocumentReference converted = underTest.convertToPdf(inputDocumentReference);

        assertThat(converted).isEqualTo(inputDocumentReference);
        verifyNoMoreInteractions(restTemplate, downloadService, uploadService, configuration);
    }

    @Test
    void shouldConvertNonPdfDocumentReferenceToPdf() {
        final byte[] inputDocumentBinaries = testDocumentBinaries();
        final byte[] convertedDocumentBinaries = testDocumentBinaries();
        final DocumentReference originalDocument = testDocumentReference(DOCX_FILE_NAME);

        when(downloadService.downloadDocument(originalDocument.getBinaryUrl())).thenReturn(inputDocumentBinaries);

        when(configuration.getUrl()).thenReturn(BASE_URL);
        when(configuration.getAccessKey()).thenReturn(ACCESS_KEY);

        // exact payload is tested in shouldConvertNonPdfDocumentBinariesToPdf
        when(restTemplate.exchange(
            eq(String.format("%s/rs/convert", BASE_URL)), eq(HttpMethod.POST), any(), eq(byte[].class))
        ).thenReturn(new ResponseEntity<>(convertedDocumentBinaries, HttpStatus.OK));

        final Document uploadedDocument = testDocument();
        when(uploadService.uploadPDF(convertedDocumentBinaries, PDF_FILE_NAME)).thenReturn(uploadedDocument);

        final DocumentReference uploadedReference = DocumentReference.buildFromDocument(uploadedDocument);

        final DocumentReference converted = underTest.convertToPdf(originalDocument);

        assertThat(converted).isEqualTo(uploadedReference);
    }

    @Test
    void shouldReturnSameDocumentBinariesIfItIsPdf() {
        final byte[] inputDocumentBinaries = testDocumentBinaries();
        final byte[] converted = underTest.convertToPdf(inputDocumentBinaries, PDF_FILE_NAME);

        assertThat(converted).isEqualTo(inputDocumentBinaries);
        verifyNoMoreInteractions(restTemplate, configuration);
    }

    @Test
    void shouldConvertNonPdfDocumentBinariesToPdf() {
        final byte[] inputDocumentBinaries = testDocumentBinaries();
        final byte[] convertedDocumentBinaries = testDocumentBinaries();

        when(configuration.getUrl()).thenReturn(BASE_URL);
        when(configuration.getAccessKey()).thenReturn(ACCESS_KEY);

        when(restTemplate.exchange(
            eq(String.format("%s/rs/convert", BASE_URL)), eq(HttpMethod.POST), any(), eq(byte[].class))
        ).thenReturn(new ResponseEntity<>(convertedDocumentBinaries, HttpStatus.OK));

        final byte[] converted = underTest.convertToPdf(inputDocumentBinaries, DOCX_FILE_NAME);

        verify(restTemplate).exchange(
            String.format("%s/rs/convert", BASE_URL), HttpMethod.POST,
            getExpectedPayload(inputDocumentBinaries, DOCX_FILE_NAME, PDF_FILE_NAME),
            byte[].class
        );

        assertThat(converted).isEqualTo(convertedDocumentBinaries);
    }

    private HttpEntity<MultiValueMap<String, Object>> getExpectedPayload(byte[] fileToBeConverted,
                                                                         String oldFilename, String newFilename) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);

        final ContentDisposition contentDisposition = ContentDisposition
            .builder("form-data")
            .name("file")
            .filename(oldFilename)
            .build();

        final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        fileMap.add(CONTENT_DISPOSITION, contentDisposition.toString());

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(fileToBeConverted, fileMap));
        body.add("outputName", newFilename);
        body.add("accessKey", ACCESS_KEY);

        return new HttpEntity<>(body, headers);
    }
}
