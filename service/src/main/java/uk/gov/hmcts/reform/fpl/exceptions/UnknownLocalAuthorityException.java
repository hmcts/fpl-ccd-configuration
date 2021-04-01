package uk.gov.hmcts.reform.fpl.exceptions;

@SuppressWarnings("squid:S110")
public class UnknownLocalAuthorityException extends RuntimeException {
    public UnknownLocalAuthorityException(String localAuthorityCode) {
        super(String.format("Local authority with code %s does not have id configured", localAuthorityCode));
    }
}
