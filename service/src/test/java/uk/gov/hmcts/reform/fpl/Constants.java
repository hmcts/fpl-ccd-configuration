package uk.gov.hmcts.reform.fpl;

@SuppressWarnings("LineLength")
public interface Constants {

    /**
     * Service auth token issued for 'fpl_case_service' valid until end of the day on 31st December 2049.
     *
     * <p>Decoded payload:
     *
     * <code>
     * {
     *   "sub": "fpl_case_service",
     *   "exp": 2524607999
     * }
     * </code>
     */
    String SERVICE_AUTH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmcGxfY2FzZV9zZXJ2aWNlIiwiZXhwIjoyNTI0NjA3OTk5fQ.LLvcAFmBnIpTbmuoEBuFUzRE6prE0_1ALbCkEkY6mS0kHPQVghgWPNBeVOKUoa-w1P1IUqZ5H-6eYnDzJRxTwA";
    String DEFAULT_LA_CODE = "example";
    String DEFAULT_LA_NAME = "Example Local Authority";
    String DEFAULT_LA_ORG_ID = "ORG-LA";
    String DEFAULT_PRIVATE_ORG_ID = "ORG-EXT";
    String DEFAULT_LA_COURT = "Family Court";
    String DEFAULT_CAFCASS_COURT = "cafcass";
    String DEFAULT_CAFCASS_EMAIL = "FamilyPublicLaw+cafcass@gmail.com";
    String USER_AUTH_TOKEN = "token";
}
