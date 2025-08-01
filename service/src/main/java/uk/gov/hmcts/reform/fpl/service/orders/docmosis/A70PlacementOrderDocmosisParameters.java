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
public class A70PlacementOrderDocmosisParameters extends DocmosisParameters {

    private String localAuthorityName;
    private String localAuthorityAddress;
    private String applicationDate;
    private DocmosisChild child;

}
