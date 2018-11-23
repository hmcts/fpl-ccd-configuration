package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLTemplateProcessor;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;
import uk.gov.hmcts.reform.pdf.generator.PDFGenerator;
import uk.gov.hmcts.reform.pdf.generator.XMLContentSanitizer;
import uk.gov.hmcts.reform.pebble.AgeFilter;
import uk.gov.hmcts.reform.pebble.TodayFilter;

import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class DocumentGeneratorService {

    private final HTMLToPDFConverter converter = new HTMLToPDFConverter(
        new HTMLTemplateProcessor(new PebbleEngine.Builder()
            .strictVariables(true)
            .loader(new StringLoader())
            .cacheActive(false)
            .extension(new AbstractExtension() {
                @Override
                public Map<String, Filter> getFilters() {
                    Map<String, Filter> filters = super.getFilters();
                    return ImmutableMap.<String, Filter>builder()
                        .put("today", new TodayFilter())
                        .put("age", new AgeFilter())
                        .build();
                }
            })
            .build()),
        new PDFGenerator(),
        new XMLContentSanitizer()
    );
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
