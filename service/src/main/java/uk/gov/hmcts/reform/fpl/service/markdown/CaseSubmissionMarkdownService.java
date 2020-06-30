package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.markdown.CaseSubmissionSubstitutionData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.CASE_SUBMISSION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

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
        CaseSubmissionSubstitutionData substitutionData = CaseSubmissionSubstitutionData.builder()
            .surveyLink(surveyUrl)
            .caseName(caseData.getCaseName())
            .ccdCaseNumber(formatCCDCaseNumber(ccdCaseNumber))
            .orders(formatOrders(caseData.getOrders()))
            .build();

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

    private String formatOrders(Orders orders) {
        return orders.getOrderType().stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
    }
}
