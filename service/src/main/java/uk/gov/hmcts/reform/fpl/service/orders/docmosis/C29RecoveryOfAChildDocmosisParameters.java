package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C29RecoveryOfAChildDocmosisParameters extends DocmosisParameters {
    String orderDetails;
    String furtherDirections;
    String localAuthorityName;
    String orderHeader;
    String orderMessage;
}
