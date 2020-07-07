package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.markdown.CaseSubmissionSubstitutionData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.CASE_SUBMISSION;

@Service
public class CaseSubmissionMarkdownService extends MarkdownSubstitutionService {
    private static final String SEPARATOR = "<break>";

    private final String surveyUrl;

    @Autowired
    public CaseSubmissionMarkdownService(ObjectMapper mapper,
                                         @Value("${survey.url.caseSubmission}") String surveyUrl) {
        super(mapper);
        this.surveyUrl = surveyUrl;
    }

    public MarkdownData getMarkdownData(String caseName) {
        CaseSubmissionSubstitutionData substitutionData = CaseSubmissionSubstitutionData.builder()
            .surveyLink(surveyUrl)
            .caseName(defaultString(caseName))
            .build();

        return generateMarkdown(CASE_SUBMISSION, substitutionData);
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
