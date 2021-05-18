package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C23EPODocmosisParameters extends DocmosisParameters {
    GeneratedOrderType orderType;
    String orderDetails;
    String furtherDirections;
    String localAuthorityName;
    EPOType epoType;
    String includePhrase;
    String childrenDescription;
    String epoStartDateTime;
    String epoEndDateTime;
    String removalAddress;
    String exclusionRequirement;
}
