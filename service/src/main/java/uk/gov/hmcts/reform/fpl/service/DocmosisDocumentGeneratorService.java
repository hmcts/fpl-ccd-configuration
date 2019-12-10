package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class DocmosisDocumentGeneratorService {
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocmosisConfiguration docmosisDocumentGenerationConfiguration;

    @Autowired
    DocmosisDocumentGeneratorService(RestTemplate restTemplate,
                                     DocmosisConfiguration docmosisDocumentGenerationConfiguration) {
        this.restTemplate = restTemplate;
        this.docmosisDocumentGenerationConfiguration = docmosisDocumentGenerationConfiguration;
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData,
                                                     DocmosisTemplates docmosisTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(docmosisTemplate.getTemplate())
            .data(templateData)
            .outputFormat("pdf")
            .outputName("IGNORED")
            .accessKey(docmosisDocumentGenerationConfiguration.getAccessKey())
            .build();

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        byte[] response = null;

        try {
            response = restTemplate.exchange(docmosisDocumentGenerationConfiguration.getUrl() + "/rs/render",
                HttpMethod.POST, request, byte[].class).getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            logger.error("Docmosis document generation failed" + ex.getResponseBodyAsString());
            throw ex;
        }

        return new DocmosisDocument(docmosisTemplate.getDocumentTitle(), response);
    }

    public String generateDraftWatermarkEncodedString() {
        InputStream is = getClass().getResourceAsStream("/assets/images/draft-watermark.png");
        byte[] fileContent = new byte[0];
        try {
            fileContent = is.readAllBytes();
        } catch (IOException e) {
            log.error("Unable to generate draft water image for template.", e);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
