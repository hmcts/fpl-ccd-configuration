package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

import java.util.UUID;

public class RemovableOrderOrApplicationNotFoundException extends IllegalStateException {
    public RemovableOrderOrApplicationNotFoundException(UUID orderId) {
        super(String.format("Removable order or application with id %s not found", orderId));
    }
}
