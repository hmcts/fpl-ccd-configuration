package uk.gov.hmcts.reform.fpl.model.order.generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExclusionClause {
    private final String exclusionClauseNeeded;
    @JsonProperty("exclusionClause")
    private final String exclusionClauseText;
}
