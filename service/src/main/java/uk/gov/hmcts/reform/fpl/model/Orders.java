package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.List;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class Orders {

    @NotNull(message = "Select at least one type of order")
    private final List<OrderType> orderType;
    private final String directions;
    private final List<String> emergencyProtectionOrderDirections;
}
