package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.updateExtension;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentConversionService {
    private final RestTemplate restTemplate;
    private final DocmosisConfiguration configuration;
    private final DocumentDownloadService downloadService;
    private final UploadDocumentService uploadService;
    private static final String PDF = "pdf";

    public DocumentReference convertToPdf(DocumentReference document) {
        String filename = document.getFilename();
        if (hasExtension(filename, PDF)) {
            return document;
        }

        byte[] documentContent = downloadService.downloadDocument(document.getBinaryUrl());
        byte[] updatedContent = convertToPdf(documentContent, filename);
        Document uploadedPDF = uploadService.uploadPDF(updatedContent, updateExtension(filename, PDF));
        return DocumentReference.buildFromDocument(uploadedPDF);
    }

    public byte[] convertToPdf(byte[] documentContents, String filename) {
        if (!hasExtension(filename, PDF)) {
            return convertDocument(documentContents, filename, updateExtension(filename, PDF));
        }

        return documentContents;
    }

    private byte[] convertDocument(byte[] binaries, String oldName, String newName) {
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
