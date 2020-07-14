package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.markdown.CaseSubmissionSubstitutionData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownSubstitutionData;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.CASE_SUBMISSION;

class MarkdownSubstitutionServiceTest {
    private static final String CASE_NAME = "Gio vs FPLA";
    private static final String SURVEY_LINK = "https://cats.for.gio";

    private final ObjectMapper mapper = new ObjectMapper();
    private final MarkdownSubstitutionService markdownSubstitutionService = new MarkdownSubstitutionService(mapper) {
        @Override
        protected MarkdownData transform(String templateData) {
            return MarkdownData.builder().body(templateData).build();
        }
    };

    @Test
    void shouldSubstituteValuesProvidedInSubstitutionData() {
        CaseSubmissionSubstitutionData data = CaseSubmissionSubstitutionData.builder()
            .caseName(CASE_NAME)
            .surveyLink(SURVEY_LINK)
            .build();

        MarkdownData markdownData = markdownSubstitutionService.generateMarkdown(CASE_SUBMISSION, data);

        assertThat(markdownData).isEqualTo(expectedData(CASE_NAME, SURVEY_LINK));
    }

    @Test
    void shouldNotReplaceFieldsWhenDataIsNotProvided() {
        CaseSubmissionSubstitutionData data = CaseSubmissionSubstitutionData.builder()
            .caseName(CASE_NAME)
            .build();

        MarkdownData markdownData = markdownSubstitutionService.generateMarkdown(CASE_SUBMISSION, data);

        assertThat(markdownData).isEqualTo(expectedData(CASE_NAME, "${surveyLink}"));
    }

    @Test
    void shouldIgnoreVariablesNotInMarkdownTemplate() {
        TestSubstitutionData data = new TestSubstitutionData("pls don't appear");

        MarkdownData markdownData = markdownSubstitutionService.generateMarkdown(CASE_SUBMISSION, data);

        assertThat(markdownData).isEqualTo(expectedData("${caseName}", "${surveyLink}"));
    }

    private MarkdownData expectedData(String caseName, String surveyLink) {
        return MarkdownData.builder()
            .body(format("%s\n<break>\n%s\n", caseName, surveyLink))
            .build();
    }

    static class TestSubstitutionData implements MarkdownSubstitutionData {
        private final String notInFile;

        public TestSubstitutionData(String notInFile) {
            this.notInFile = notInFile;
        }

        public String getNotInFile() {
            return notInFile;
        }
    }
}
