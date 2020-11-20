package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalActions {
    private final CMOOrderRemovalAction cmoOrderRemovalAction;
    private final OtherOrderRemovalAction otherOrderRemovalAction;

    public List<OrderRemovalAction> getActions() {
        return List.of(
            cmoOrderRemovalAction,
            otherOrderRemovalAction
        );
    }


}
