package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import static org.assertj.core.api.Assertions.assertThat;

class CaseSubmissionMarkdownServiceTest {
    private static final String CASE_NAME = "Corona vs World";
    private static final String SURVEY_URL = "https://fake.url";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CaseSubmissionMarkdownService service = new CaseSubmissionMarkdownService(objectMapper, SURVEY_URL);

    @Test
    void shouldSplitOnSeparator() {
        MarkdownData markdownData = service.getMarkdownData(CASE_NAME);

        assertThat(markdownData).isEqualTo(buildExpectedData());
    }

    @Test
    void shouldRemoveInitialBlankLinesWhenCaseNameNotProvided() {
        MarkdownData markdownData = service.getMarkdownData(null);

        assertThat(markdownData).isEqualTo(buildExpectedData(""));
    }

    private MarkdownData buildExpectedData() {
        return buildExpectedData(CASE_NAME);
    }

    private MarkdownData buildExpectedData(String caseName) {
        return MarkdownData.builder()
            .header(caseName.trim())
            .body(SURVEY_URL)
            .build();
    }
}
