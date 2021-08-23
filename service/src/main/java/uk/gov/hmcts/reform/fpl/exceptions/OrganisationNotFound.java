package uk.gov.hmcts.reform.fpl.exceptions;

public class OrganisationNotFound extends RuntimeException {

    public OrganisationNotFound(String message) {
        super(message);
    }
}
