package uk.gov.hmcts.reform.fpl.exceptions;

public class OrganisationPolicyNotFound extends RuntimeException {

    public OrganisationPolicyNotFound(String message) {
        super(message);
    }
}
