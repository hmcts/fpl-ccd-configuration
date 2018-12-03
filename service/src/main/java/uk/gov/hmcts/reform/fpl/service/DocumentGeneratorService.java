package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class DocumentGeneratorService {

    private final HTMLToPDFConverter converter;
    private final DocumentTemplates templates;
    private final ObjectMapper mapper;

    @Autowired
    public DocumentGeneratorService(HTMLToPDFConverter converter, DocumentTemplates templates, ObjectMapper mapper) {
        this.converter = converter;
        this.templates = templates;
        this.mapper = mapper;
    }

    public byte[] generateSubmittedFormPDF(CaseDetails caseDetails) {
        Map<String, Object> context = mapper.convertValue(caseDetails, Map.class);

        byte[] template = templates.getHtmlTemplate();

        return converter.convert(template, context);
    }
}
