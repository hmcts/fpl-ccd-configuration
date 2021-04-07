package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.order.Order;

@Value
@Builder
@Jacksonized
public class ManageOrdersEventData {
    Order manageOrdersType;
}
