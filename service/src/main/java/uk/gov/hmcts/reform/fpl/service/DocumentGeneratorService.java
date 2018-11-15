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

    private final HTMLToPDFConverter converter = new HTMLToPDFConverter();
    private final DocumentTemplates documentTemplates;
    private final ObjectMapper mapper;

    @Autowired
    public DocumentGeneratorService(DocumentTemplates documentTemplates, ObjectMapper mapper) {
        this.documentTemplates = documentTemplates;
        this.mapper = mapper;
    }

    public byte[] generateSubmittedFormPDF(CaseDetails caseDetails) {
        Map<String, Object> context = mapper.convertValue(caseDetails, Map.class);

        byte[] template = documentTemplates.getHtmlTemplate();

        return converter.convert(template, context);
    }
}
