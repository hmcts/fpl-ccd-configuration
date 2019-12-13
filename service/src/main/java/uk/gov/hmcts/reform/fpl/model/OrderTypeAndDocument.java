package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
//1st page of create an order event: user selects type, document is hidden until check answers page
public class OrderTypeAndDocument {
    private final GeneratedOrderType type;
    private final DocumentReference document;
}
