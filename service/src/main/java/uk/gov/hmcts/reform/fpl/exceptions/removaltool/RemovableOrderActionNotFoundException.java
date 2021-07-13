package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

public class RemovableOrderActionNotFoundException extends IllegalStateException {
    public RemovableOrderActionNotFoundException() {
        super("Removable order action not found");
    }

    public RemovableOrderActionNotFoundException(String orderType) {
        super(String.format("Removable order action for order of type %s not found", orderType));
    }
}
