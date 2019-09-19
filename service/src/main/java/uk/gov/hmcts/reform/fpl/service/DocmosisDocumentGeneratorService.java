package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisDocumentGenerationConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisRequest;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.config.DocmosisDocumentGenerationConfiguration.DocmosisConfig;

@Service
public class DocmosisDocumentGeneratorService {
    private final RestTemplate restTemplate;
    private final String tornadoUrl;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String tornadoAccessKey;

    @Autowired
    DocmosisDocumentGeneratorService(RestTemplate restTemplate,
                                     @Value("${docmosis.tornado.url}") String tornadoUrl,
                                     @Value("${docmosis.api.key}") String tornadoAccessKey) {
        this.restTemplate = restTemplate;
        this.tornadoUrl = tornadoUrl + "/rs/render";
        this.tornadoAccessKey = tornadoAccessKey;
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, String> templateData,
                                                     DocmosisTemplates docmosisTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisConfig docmosisConfig = new
            DocmosisDocumentGenerationConfiguration().docmosisConfig(tornadoUrl, tornadoAccessKey);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(docmosisTemplate.getTemplate())
            .data(templateData)
            .outputFormat("pdf")
            .outputName("IGNORED")
            .accessKey(docmosisConfig.getAccessKey())
            .build();

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        byte[] response = null;

        try {
            response = restTemplate.exchange(docmosisConfig.getUrl(), HttpMethod.POST, request, byte[].class).getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            logger.error("Docmosis document generation failed" + ex.getResponseBodyAsString());
            throw ex;
        }

        return new DocmosisDocument(docmosisTemplate.getDocumentTitle(), response);
    }
}
