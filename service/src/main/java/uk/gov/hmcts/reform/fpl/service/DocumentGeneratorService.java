package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.Map;

@Service
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

    @SafeVarargs
    public final byte[] generateSubmittedFormPDF(CaseDetails caseDetails, Map.Entry<String, ?>... extraContextEntries) {
        Map<String, Object> context = mapper.convertValue(caseDetails, new TypeReference<Map<String, Object>>() {});

        for (Map.Entry<String, ?> entry : extraContextEntries) {
            context.put(entry.getKey(), entry.getValue());
        }

        byte[] template = templates.getHtmlTemplate();

        return converter.convert(template, context);
    }
}
