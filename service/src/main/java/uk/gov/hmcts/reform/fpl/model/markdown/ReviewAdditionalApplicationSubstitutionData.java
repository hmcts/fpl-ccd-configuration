package uk.gov.hmcts.reform.fpl.model.markdown;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewAdditionalApplicationSubstitutionData implements MarkdownSubstitutionData{
    private final String caseName;
}
