package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownSubstitutionData;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.util.Map;

import static org.apache.commons.text.StringSubstitutor.replace;

@RequiredArgsConstructor
public abstract class MarkdownSubstitutionService {
    private static final String TEMPLATE_DIRECTORY = "templates/markdown";

    private final ObjectMapper mapper;

    protected MarkdownData generateMarkdown(MarkdownTemplate template, MarkdownSubstitutionData markdownData) {
        String templateData = readMarkdownTemplate(template);
        templateData = substitute(templateData, markdownData);
        return transform(templateData);
    }

    protected abstract MarkdownData transform(String templateData);

    private String substitute(String templateData, MarkdownSubstitutionData markdownData) {
        return replace(templateData, mapper.<Map<String, String>>convertValue(markdownData, new TypeReference<>() {}));
    }

    private String readMarkdownTemplate(MarkdownTemplate template) {
        return ResourceReader.readString(String.format("%s/%s.md", TEMPLATE_DIRECTORY, template.getFile()));
    }
}
