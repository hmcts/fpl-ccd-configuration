package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownSubstitutionData;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.util.Map;

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
        Map<String, String> data = mapper.convertValue(markdownData, new TypeReference<>() {});

        for (Map.Entry<String, String> entry : data.entrySet()) {
            templateData = StringUtils.replace(templateData, toVariable(entry.getKey()), entry.getValue());
        }

        return templateData;
    }

    private String toVariable(String variableName) {
        return "{{" + variableName + "}}";
    }

    private String readMarkdownTemplate(MarkdownTemplate template) {
        return ResourceReader.readString(String.format("%s/%s.md", TEMPLATE_DIRECTORY, template.getFile()));
    }
}
