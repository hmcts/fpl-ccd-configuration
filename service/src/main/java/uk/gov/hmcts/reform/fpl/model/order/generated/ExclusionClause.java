package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExclusionClause {
    private final String exclusionClauseNeeded;
    private final String exclusionClauseText;
}
