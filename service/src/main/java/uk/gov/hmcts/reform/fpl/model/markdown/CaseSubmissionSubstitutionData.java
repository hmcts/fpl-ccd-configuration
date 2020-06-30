package uk.gov.hmcts.reform.fpl.model.markdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaseSubmissionSubstitutionData implements MarkdownSubstitutionData {
    @JsonProperty(value = "{{caseName}}")
    private final String caseName;
    @JsonProperty(value = "{{ccdCaseNumber}}")
    private final String ccdCaseNumber;
    @JsonProperty(value = "{{orders}}")
    private final String orders;
    @JsonProperty(value = "{{surveyLink}}")
    private final String surveyLink;
}
