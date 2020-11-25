package uk.gov.hmcts.reform.fpl.exceptions.removeorder;

import java.util.UUID;

public class RemovableOrderActionNotFoundException extends IllegalStateException {
    public RemovableOrderActionNotFoundException() {
        super("Removable order action not found");
    }

    public RemovableOrderActionNotFoundException(UUID orderId) {
        super(String.format("Removable order action not found for order %s", orderId));
    }
}
