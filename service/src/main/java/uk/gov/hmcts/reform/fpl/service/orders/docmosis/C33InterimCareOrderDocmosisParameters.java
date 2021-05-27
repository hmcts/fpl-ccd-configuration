package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C33InterimCareOrderDocmosisParameters extends DocmosisParameters {
    GeneratedOrderType orderType;
    String orderDetails;
    String furtherDirections;
    String exclusionClause;
    String localAuthorityName;
}
