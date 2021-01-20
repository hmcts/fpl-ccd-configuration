package uk.gov.hmcts.reform.fpl.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDept")
public class UnknownLocalAuthorityDomainException extends LogAsWarningException {
    public UnknownLocalAuthorityDomainException(String message) {
        super("The email address was not linked to a known Local Authority", message);
    }
}
