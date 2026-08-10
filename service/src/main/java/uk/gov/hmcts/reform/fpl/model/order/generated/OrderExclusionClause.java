package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ExclusionClause", generate = true)
@Data
@Builder
public class OrderExclusionClause {
    @CCD(label = "Is there an exclusion clause on the order?", typeOverride = FieldType.YesOrNo)
    private final String exclusionClauseNeeded;
    @CCD(
            label = "Enter exclusion clause",
            showCondition = "exclusionClauseNeeded = \"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String exclusionClause;
}
