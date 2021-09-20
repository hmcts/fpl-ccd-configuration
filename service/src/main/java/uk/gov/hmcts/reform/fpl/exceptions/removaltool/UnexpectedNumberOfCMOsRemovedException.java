package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;

import java.util.UUID;

public class UnexpectedNumberOfCMOsRemovedException extends AboutToStartOrSubmitCallbackException {

    private static final String CMO_ERROR_MESSAGE = "Email the help desk at dcd-familypubliclawservicedesk@hmcts.net to"
        + " remove this order. Quoting CMO %s, and the hearing it was added for.";

    public UnexpectedNumberOfCMOsRemovedException(UUID removedOrderID, String message) {
        super(String.format(CMO_ERROR_MESSAGE, removedOrderID), message);
    }
}
