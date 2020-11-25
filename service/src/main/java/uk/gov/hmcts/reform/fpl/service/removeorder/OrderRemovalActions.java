package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalActions {
    private final CMOOrderRemovalAction cmoOrderRemovalAction;
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;

    public OrderRemovalAction getAction(UUID removedOrderId,
                                        RemovableOrder removableOrder) {
        return getActions()
            .stream()
            .filter(orderRemovalAction -> orderRemovalAction.isAccepted(removableOrder))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(format("Action not found for order %s", removedOrderId)));
    }

    private List<OrderRemovalAction> getActions() {
        return List.of(
            cmoOrderRemovalAction,
            generatedOrderRemovalAction
        );
    }
}
