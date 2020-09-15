package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentConversionService {
    private final RestTemplate restTemplate;
    private final DocmosisConfiguration configuration;
    private final DocumentDownloadService documentDownloadService;
    private final UploadDocumentService uploadDocumentService;

    public DocumentReference convertToPdf(DocumentReference document) {
        //If document is already a pdf, do not convert. Will still 'convert' pdfs that have do not have an extension.
        if (FilenameUtils.getExtension(document.getFilename()).equals("pdf")) {
            return document;
        } else {
            final String oldName = document.getFilename();
            final String newName = FilenameUtils.removeExtension(document.getFilename()).concat(".pdf");

            byte[] documentContent = documentDownloadService.downloadDocument(document.getBinaryUrl());
            byte[] convertedDocument = convertDocument(documentContent, oldName, newName);

            return buildFromDocument(uploadDocumentService.uploadPDF(convertedDocument, newName));
        }
    }

    public byte[] convertDocument(byte[] binaries, String oldName, String newName) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);

        final ContentDisposition contentDisposition = ContentDisposition
            .builder("form-data")
            .name("file")
            .filename(oldName)
            .build();

        final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        fileMap.add(CONTENT_DISPOSITION, contentDisposition.toString());

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(binaries, fileMap));
        body.add("outputName", newName);
        body.add("accessKey", configuration.getAccessKey());

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate
                .exchange(
                    configuration.getUrl() + "/rs/convert",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class)
                .getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Document conversion failed" + ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
