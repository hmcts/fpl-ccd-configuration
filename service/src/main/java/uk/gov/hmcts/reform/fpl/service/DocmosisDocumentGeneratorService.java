package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisDocumentGeneratorService {
    private final RestTemplate restTemplate;
    private final DocmosisConfiguration configuration;
    private final ObjectMapper mapper;

    public DocmosisDocument generatedDocmosisDocument(DocmosisData templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData.toMap(mapper), template);
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(template.getTemplate())
            .data(templateData)
            .outputFormat("pdf")
            .outputName("IGNORED")
            .accessKey(configuration.getAccessKey())
            .build();

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        byte[] response;

        try {
            response = restTemplate.exchange(configuration.getUrl() + "/rs/render",
                HttpMethod.POST, request, byte[].class).getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Docmosis document generation failed" + ex.getResponseBodyAsString());
            throw ex;
        }

        return new DocmosisDocument(template.getDocumentTitle(), response);
    }
}
