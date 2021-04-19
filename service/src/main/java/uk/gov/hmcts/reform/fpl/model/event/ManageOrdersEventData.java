package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class ManageOrdersEventData {
    Order manageOrdersType;
    LocalDate manageOrdersApprovalDate;
    LocalDateTime manageOrdersApprovalDateTime;
    LocalDateTime manageOrdersEndDateTime;
    String manageOrdersFurtherDirections;
}
