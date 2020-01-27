package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.HTMLToPDFConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentGeneratorService {

    private final HTMLToPDFConverter converter;
    private final DocumentTemplates templates;
    private final ObjectMapper mapper;

    @Autowired
    public DocumentGeneratorService(HTMLToPDFConverter converter,
                                    DocumentTemplates templates,
                                    ObjectMapper mapper) {
        this.converter = converter;
        this.templates = templates;
        this.mapper = mapper;
    }

    @SafeVarargs
    public final byte[] generateSubmittedFormPDF(CaseDetails caseDetails, Map.Entry<String, ?>... extraContextEntries) {
        Map<String, Object> context = mapper.convertValue(populateEmptyCollections(caseDetails),
            new TypeReference<>() {});

        for (Map.Entry<String, ?> entry : extraContextEntries) {
            context.put(entry.getKey(), entry.getValue());
        }

        byte[] template = templates.getHtmlTemplate();

        return converter.convert(template, context);
    }

    private CaseDetails populateEmptyCollections(CaseDetails caseDetails) {
        if (caseDetails != null) {
            Map<String, Object> dataCopy = new HashMap<>(caseDetails.getData());
            dataCopy.putIfAbsent("children1", collectionWithEmptyElement());
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
