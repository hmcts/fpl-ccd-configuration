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

    @Autowired
    public DocumentGeneratorService(DocumentTemplates documentTemplates) {
        this.documentTemplates = documentTemplates;
    }

    public byte[] documentGenerator(CaseDetails caseDetails) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.convertValue(caseDetails, Map.class);

        byte[] template = documentTemplates.getHtmlTemplate();

        return converter.convert(template, map);
    }
}
