package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderExclusionClause {
    private final String exclusionClauseNeeded;
    private final String exclusionClause;
}
