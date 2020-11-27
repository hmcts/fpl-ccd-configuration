package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderActionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalActions {
    private final CMORemovalAction cmoRemovalAction;
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;
    private final SDORemovalAction sdoRemovalAction;

    public OrderRemovalAction getAction(RemovableOrder removableOrder) {
        return getActions()
            .filter(orderRemovalAction -> orderRemovalAction.isAccepted(removableOrder))
            .findFirst()
            .orElseThrow(() -> new RemovableOrderActionNotFoundException(cmoRemovalAction.getClass()
                .getSimpleName()));
    }

    private Stream<OrderRemovalAction> getActions() {
        return Stream.of(
            cmoRemovalAction,
            generatedOrderRemovalAction,
            sdoRemovalAction
        );
    }
}
