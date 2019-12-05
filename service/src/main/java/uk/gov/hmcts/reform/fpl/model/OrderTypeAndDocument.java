package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.FinalOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
public class OrderTypeAndDocument {
    private final FinalOrderType finalOrderType;
    private final DocumentReference document;
}
