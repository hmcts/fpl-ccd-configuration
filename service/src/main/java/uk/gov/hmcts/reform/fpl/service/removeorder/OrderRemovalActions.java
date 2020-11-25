package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalActions {
    private final CMOOrderRemovalAction cmoOrderRemovalAction;
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;

    public OrderRemovalAction getAction(UUID removedOrderId,
                                        RemovableOrder removableOrder) {
        return getActions()
            .filter(orderRemovalAction -> orderRemovalAction.isAccepted(removableOrder))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(format("Action not found for order %s", removedOrderId)));
    }

    private Stream<OrderRemovalAction> getActions() {
        return Stream.of(
            cmoOrderRemovalAction,
            generatedOrderRemovalAction
        );
    }
}
