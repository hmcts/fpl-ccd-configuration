package uk.gov.hmcts.reform.fpl.model.markdown;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class CaseSubmissionSubstitutionData implements MarkdownSubstitutionData {
    @JsonProperty(value = "{{caseName}}")
    private String caseName;
    @JsonProperty(value = "{{ccdCaseNumber}}")
    private String ccdCaseNumber;
    @JsonProperty(value = "{{orders}}")
    private String orders;
    @JsonProperty(value = "{{ctscInfo}}")
    private String ctscInfo;
    @JsonProperty(value = "{{surveyLink}}")
    private String surveyLink;
}
