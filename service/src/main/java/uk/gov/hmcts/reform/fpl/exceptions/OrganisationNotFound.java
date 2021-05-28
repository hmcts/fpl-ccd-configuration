package uk.gov.hmcts.reform.fpl.exceptions;

public class OrganisationNotFound extends RuntimeException {

    public OrganisationNotFound(String organisationId) {
        super(String.format("Organisation %s not found", organisationId));
    }
}
