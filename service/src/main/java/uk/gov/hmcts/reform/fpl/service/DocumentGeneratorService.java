package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.email.content.TornadoDocumentTemplates;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentGeneratorService {

    private final HTMLToPDFConverter converter;
    private final DocumentTemplates templates;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;
    private final String tornadoUrl;


    @Autowired
    public DocumentGeneratorService(HTMLToPDFConverter converter,
                                    DocumentTemplates templates,
                                    ObjectMapper mapper,
                                    RestTemplate restTemplate,
                                    @Value("${docmosis.tornado.url}") String tornadoUrl) {
        this.converter = converter;
        this.templates = templates;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.tornadoUrl = tornadoUrl + "/rs/render";
    }

    @SafeVarargs
    public final byte[] generateSubmittedFormPDF(CaseDetails caseDetails, Map.Entry<String, ?>... extraContextEntries) {
        Map<String, Object> context = mapper.convertValue(populateEmptyCollections(caseDetails),
            new TypeReference<Map<String, Object>>() {});

        for (Map.Entry<String, ?> entry : extraContextEntries) {
            context.put(entry.getKey(), entry.getValue());
        }

        byte[] template = templates.getHtmlTemplate();

        return converter.convert(template, context);
    }

    public final byte[] generatePdf(Map<String,String> docGenerationData, TornadoDocumentTemplates documentTemplates) {

        // create request entity
        // set request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = new DocmosisRequest(documentTemplates.getDocumentTemplate(),
             docGenerationData);

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);
        byte[] response = null;
        try {
            response = restTemplate.exchange(tornadoUrl, HttpMethod.POST, request, byte[].class).getBody();
        } catch (HttpClientErrorException.BadRequest ex) {
            System.out.println("body" +  ex.getResponseBodyAsString());
        }
        return response;
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

    private CaseDetails populateEmptyCollections(CaseDetails caseDetails) {
        if (caseDetails != null) {
            Map<String, Object> dataCopy = new HashMap<>(caseDetails.getData());
            dataCopy.putIfAbsent("applicants", collectionWithEmptyElement());
            dataCopy.putIfAbsent("respondents1", collectionWithEmptyElement());

            return caseDetails.toBuilder().data(dataCopy).build();
        }
        return null;
    }

    private <T> List<Element<T>> collectionWithEmptyElement() {
        return ImmutableList.of(new Element<>(UUID.randomUUID(), null));
    }
}
