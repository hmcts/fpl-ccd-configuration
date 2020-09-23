package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ExtendWith(MockitoExtension.class)
class DocumentConversionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private DocmosisConfiguration configuration = new DocmosisConfiguration("baseUrl", "accessKey");

    @InjectMocks
    private DocumentConversionService documentConversionService;

    @Test
    void shouldReturnSameDocumentIfItIsPdf() {
        byte[] inputDocumentBinaries = TestDataHelper.testDocumentBinaries();
        final DocumentReference inputDocumentReference = DocumentReference.builder()
            .filename("cmo.pdf")
            .build();

        final byte[] converted = documentConversionService.convertToPdf(inputDocumentBinaries,
            inputDocumentReference.getFilename());

        assertThat(converted).isEqualTo(inputDocumentBinaries);
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConvertNonPdfDocumentToPdf() {
        final String fileName = "cmo.docx";
        final String newFileName = "cmo.pdf";
        byte[] inputDocumentBinaries = TestDataHelper.testDocumentBinaries();
        byte[] convertedDocumentBinaries = readBytes("documents/document.pdf");
        final DocumentReference inputDocumentReference = DocumentReference.builder().filename(fileName).build();

        when(restTemplate.exchange(
            eq(String.format("%s/rs/convert", configuration.getUrl())),
            eq(HttpMethod.POST),
            any(),
            eq(byte[].class)))
            .thenReturn(new ResponseEntity(convertedDocumentBinaries, HttpStatus.OK));

        byte[] converted = documentConversionService.convertToPdf(inputDocumentBinaries,
            inputDocumentReference.getFilename());

        verify(restTemplate).exchange(
            String.format("%s/rs/convert", configuration.getUrl()),
            HttpMethod.POST,
            getExpectedPayload(inputDocumentBinaries, fileName, newFileName),
            byte[].class);

        assertThat(converted).isEqualTo(convertedDocumentBinaries);
    }

    private HttpEntity getExpectedPayload(byte[] fileToBeConverted, String oldFilename, String newFilename) {
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
        body.add("accessKey", configuration.getAccessKey());

        return new HttpEntity<>(body, headers);
    }
}
