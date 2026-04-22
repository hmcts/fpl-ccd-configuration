package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MarkdownTemplate {
    CASE_SUBMISSION("caseSubmission"),
    REVIEW_ADDITIONAL_APPLICATION("reviewAdditionalApplication");

    private final String file;
}
