package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOAddress;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOType;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEnteredEPOExcluded;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;

@Data
@Builder(toBuilder = true)
@HasEPOAddress
@HasEPOType
@HasEnteredEPOExcluded
@Jacksonized
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
    private final EPOType epoType;
    private final String excluded;
    private final Address address;

    public boolean orderContainsEPO() {
        return this.getOrderType().contains(EMERGENCY_PROTECTION_ORDER);
    }

    public boolean isDischargeOfCareOrder() {
        return List.of(OrderType.OTHER).equals(this.getOrderType());
    }
}
