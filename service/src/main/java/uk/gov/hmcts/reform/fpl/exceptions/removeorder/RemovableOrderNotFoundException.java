package uk.gov.hmcts.reform.fpl.exceptions.removeorder;

import java.util.UUID;

public class RemovableOrderNotFoundException extends IllegalStateException {
    public RemovableOrderNotFoundException() {
        super("Removable order not found");
    }

    public RemovableOrderNotFoundException(UUID orderId) {
        super(String.format("Removable order or application with id %s not found", orderId));
    }
}
