package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.markdown.ReviewAdditionalApplicationSubstitutionData;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.REVIEW_ADDITIONAL_APPLICATION;

@Service
public class ReviewAdditionalApplicationMarkdownService extends MarkdownSubstitutionService {
    private static final String SEPARATOR = "<break>";

    @Autowired
    public ReviewAdditionalApplicationMarkdownService(ObjectMapper mapper) {
        super(mapper);
    }

    public MarkdownData getMarkdownData(String caseName) {
        ReviewAdditionalApplicationSubstitutionData substitutionData = ReviewAdditionalApplicationSubstitutionData.builder()
            .caseName(defaultString(caseName))
            .build();

        return generateMarkdown(REVIEW_ADDITIONAL_APPLICATION, substitutionData);
    }

    @Override
    protected MarkdownData transform(String templateData) {
        List<String> list = Splitter.on(SEPARATOR).limit(2).splitToList(templateData);

        return MarkdownData.builder()
            .header(list.get(0).strip())
            .body(list.get(1).strip())
            .build();
    }
}
