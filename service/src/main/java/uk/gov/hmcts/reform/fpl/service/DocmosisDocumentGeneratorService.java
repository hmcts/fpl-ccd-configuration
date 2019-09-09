package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.util.Map;

@Service
public class DocmosisDocumentGeneratorService {
    private final RestTemplate restTemplate;
    private final String tornadoUrl;

    @Autowired
    DocmosisDocumentGeneratorService(RestTemplate restTemplate,
                                     @Value("${docmosis.tornado.url}") String tornadoUrl) {
        this.restTemplate = restTemplate;
        this.tornadoUrl = tornadoUrl + "/rs/render";
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, String> templateData,
                                                     DocmosisTemplates docmosisTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = new DocmosisRequest(docmosisTemplate.getTemplate(),
            templateData);

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        byte[] response = null;

        try {
            response = restTemplate.exchange(tornadoUrl, HttpMethod.POST, request, byte[].class).getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new RuntimeException("Docmosis document generation failed" + ex.getResponseBodyAsString());
        }

        return new DocmosisDocument(docmosisTemplate.getDocumentTitle(), response);
    }

    static class DocmosisRequest {
        private final String templateName;
        private final String outputFormat;
        private final String outputName;
        private final Map<String, String> data;

        DocmosisRequest(String templateName, Map<String, String> data) {
            this.templateName = templateName;
            this.data = data;
            this.outputFormat = "pdf";
            this.outputName = "IGNORED";
        }

        public String getOutputName() {
            return outputName;
        }

        public String getOutputFormat() {
            return outputFormat;
        }

        public String getTemplateName() {
            return templateName;
        }

        public Map<String, String> getData() {
            return data;
        }
    }
}
