package uk.gov.hmcts.reform.fpl.exceptions.removeorder;

import java.util.UUID;

public class RemovableOrderOrApplicationNotFoundException extends IllegalStateException {
    public RemovableOrderOrApplicationNotFoundException() {
        super("Removable order or application not found");
    }

    public RemovableOrderOrApplicationNotFoundException(UUID orderId) {
        super(String.format("Removable order or application with id %s not found", orderId));
    }
}
