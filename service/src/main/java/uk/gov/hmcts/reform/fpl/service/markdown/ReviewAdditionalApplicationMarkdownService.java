package uk.gov.hmcts.reform.fpl.service.markdown;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions;
import uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.markdown.ReviewAdditionalApplicationSubstitutionData;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPLICANT_CHANGE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.REVIEW_ADDITIONAL_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL_NO_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.MarkdownTemplate.REVIEW_ADDITIONAL_APPLICATION_NO_CTSC;

@Service
public class ReviewAdditionalApplicationMarkdownService extends MarkdownSubstitutionService {
    private static final String SEPARATOR = "<break>";

    @Autowired
    public ReviewAdditionalApplicationMarkdownService(ObjectMapper mapper) {
        super(mapper);
    }

    public MarkdownData getMarkdownData(String caseName, boolean isConfidential) {
        return getMarkdownData(caseName, isConfidential, null);
    }

    public MarkdownData getMarkdownData(String caseName,
                                        boolean isConfidential,
                                        ApproveAdditionalAppOptions approveAdditionalAppOption) {
        ReviewAdditionalApplicationSubstitutionData substitutionData =
            ReviewAdditionalApplicationSubstitutionData.builder()
            .caseName(defaultString(caseName))
            .build();

        return generateMarkdown(selectTemplate(isConfidential, approveAdditionalAppOption), substitutionData);
    }

    private MarkdownTemplate selectTemplate(boolean isConfidential,
                                            ApproveAdditionalAppOptions approveAdditionalAppOption) {
        if (APPLICANT_CHANGE_ORDER == approveAdditionalAppOption) {
            return isConfidential
                ? REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL_NO_CTSC
                : REVIEW_ADDITIONAL_APPLICATION_NO_CTSC;
        }

        return isConfidential ? REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL : REVIEW_ADDITIONAL_APPLICATION;
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
