package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

import uk.gov.hmcts.reform.fpl.exceptions.AboutToStartOrSubmitCallbackException;

import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.controllers.RemovalToolController.CMO_ERROR_MESSAGE;

public class UnexpectedNumberOfCMOsRemovedException extends AboutToStartOrSubmitCallbackException {

    public UnexpectedNumberOfCMOsRemovedException(UUID removedOrderID, String message) {
        super(String.format(CMO_ERROR_MESSAGE, removedOrderID), message);
    }
}
