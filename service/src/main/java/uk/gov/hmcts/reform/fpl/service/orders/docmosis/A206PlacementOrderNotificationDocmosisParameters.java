package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class A206PlacementOrderNotificationDocmosisParameters extends DocmosisParameters {

    String serialNumber;
    DocmosisChild child;

}
