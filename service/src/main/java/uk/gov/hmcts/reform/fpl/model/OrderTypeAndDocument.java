package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.FinalOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
//1st page of create an order event: user selects type, document is hidden until check answers page
public class OrderTypeAndDocument {
    private final FinalOrderType finalOrderType;
    private final DocumentReference document;
}
