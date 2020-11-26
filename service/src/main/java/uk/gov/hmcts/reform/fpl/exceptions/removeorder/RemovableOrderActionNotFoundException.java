package uk.gov.hmcts.reform.fpl.exceptions.removeorder;

public class RemovableOrderActionNotFoundException extends IllegalStateException {
    public RemovableOrderActionNotFoundException() {
        super("Removable order action not found");
    }

    public RemovableOrderActionNotFoundException(String orderRemovalAction) {
        super(String.format("Removable order action %s not found", orderRemovalAction));
    }
}
