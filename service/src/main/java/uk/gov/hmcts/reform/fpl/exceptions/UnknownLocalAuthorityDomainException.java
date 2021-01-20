package uk.gov.hmcts.reform.fpl.exceptions;

@SuppressWarnings("squid:S110")
public class UnknownLocalAuthorityDomainException extends LogAsWarningException {
    public UnknownLocalAuthorityDomainException(String message) {
        super("The email address was not linked to a known Local Authority", message);
    }
}
