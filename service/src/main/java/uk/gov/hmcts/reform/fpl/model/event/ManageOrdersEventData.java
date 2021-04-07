package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.order.Order;

@Value
@Builder
public class ManageOrdersEventData {
    Order manageOrdersType;

}
