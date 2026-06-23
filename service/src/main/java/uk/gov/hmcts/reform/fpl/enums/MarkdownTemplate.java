package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MarkdownTemplate {
    CASE_SUBMISSION("caseSubmission"),
    REVIEW_ADDITIONAL_APPLICATION("reviewAdditionalApplication"),
    REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL("reviewAdditionalApplicationConfidential"),
    REVIEW_ADDITIONAL_APPLICATION_NO_CTSC("reviewAdditionalApplicationNoCtsc"),
    REVIEW_ADDITIONAL_APPLICATION_CONFIDENTIAL_NO_CTSC("reviewAdditionalApplicationConfidentialNoCtsc");

    private final String file;
}
