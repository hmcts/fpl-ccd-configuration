package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class Orders {
    @NotNull(message = "Select at least one type of order")
    @Size(min = 1, message = "Select at least one type of order")
    private final List<OrderType> orderType;
    private final List<EmergencyProtectionOrdersType> emergencyProtectionOrders;
    private final String directions;
    private final List<EmergencyProtectionOrderDirectionsType> emergencyProtectionOrderDirections;
    private final String otherOrder;
    private final String emergencyProtectionOrderDetails;
    private final String emergencyProtectionOrderDirectionDetails;
    private final String directionDetails;
}
