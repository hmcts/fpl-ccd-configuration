package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.markdown.CaseSubmissionSubstitutionData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.CASE_SUBMISSION;

@Service
public class CaseSubmissionMarkdownService extends MarkdownSubstitutionService {
    private static final String SEPARATOR = "<break>";

    private final String surveyUrl;

    @Autowired
    public CaseSubmissionMarkdownService(ObjectMapper mapper,
                                         @Value("surveys.url.caseSubmission") String surveyUrl) {
        super(mapper);
        this.surveyUrl = surveyUrl;
    }

    public MarkdownData getMarkdownData(CaseData caseData, Long ccdCaseNumber) {
        CaseSubmissionSubstitutionData substitutionData = new CaseSubmissionSubstitutionData()
            .setSurveyLink(surveyUrl)
            .setCaseName(caseData.getCaseName())
            .setCcdCaseNumber(String.valueOf(ccdCaseNumber))
            .setOrders("caseData.getOrders()")
            .setCtscInfo("");

        return generateMarkdown(CASE_SUBMISSION, substitutionData);
    }

    @Override
    protected MarkdownData transform(String templateData) {
        List<String> list = Splitter.on(SEPARATOR).limit(2).splitToList(templateData);

        return MarkdownData.builder()
            .header(list.get(0))
            .body(list.get(1))
            .build();
    }
}
