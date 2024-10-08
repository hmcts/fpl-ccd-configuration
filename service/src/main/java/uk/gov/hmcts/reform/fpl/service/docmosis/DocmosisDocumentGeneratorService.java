package uk.gov.hmcts.reform.fpl.service.docmosis;

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
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisRequest;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocmosisDocumentGeneratorService {
    private final RestTemplate restTemplate;
    private final DocmosisConfiguration configuration;
    private final ObjectMapper mapper;

    public DocmosisDocument generateDocmosisDocument(DocmosisData templateData, DocmosisTemplates template,
                                                     RenderFormat format, Language language) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, format, language);
    }

    public DocmosisDocument generateDocmosisDocument(DocmosisData templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData.toMap(mapper), template);
    }

    public DocmosisDocument generateDocmosisDocument(DocmosisData templateData, DocmosisTemplates template,
                                                     RenderFormat format) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, format, Language.ENGLISH);
    }

    // REFACTOR: 08/04/2021 Remove this method in subsequent PR
    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData, template, RenderFormat.PDF, Language.ENGLISH);
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template,
                                                     RenderFormat format, Language language) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(template.getTemplate(language))
            .data(templateData)
            .outputFormat(format.getExtension())
            .outputName("IGNORED")
            .accessKey(configuration.getAccessKey())
            .build();

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            byte[] response = restTemplate.exchange(
                configuration.getUrl() + "/api/render", HttpMethod.POST, request, byte[].class)
                .getBody();
            return new DocmosisDocument(template.getDocumentTitle(), response);
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Docmosis document generation failed" + ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
