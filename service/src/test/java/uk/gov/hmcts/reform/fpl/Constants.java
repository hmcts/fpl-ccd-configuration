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
    String LOCAL_AUTHORITY_1_CODE = "test1";
    String LOCAL_AUTHORITY_2_CODE = "test2";
    String LOCAL_AUTHORITY_1_NAME = "Test 1 Local Authority";
    String LOCAL_AUTHORITY_2_NAME = "Test 2 Local Authority";
    String LOCAL_AUTHORITY_1_ID = "ORG-LA";
    String LOCAL_AUTHORITY_2_ID = "ORG-LA-2";
    String LOCAL_AUTHORITY_1_INBOX = "shared@test1.org.uk";
    String LOCAL_AUTHORITY_2_INBOX = "shared@test2.org.uk";
    String LOCAL_AUTHORITY_1_USER_EMAIL = "test@test1.org.uk";
    String LOCAL_AUTHORITY_2_USER_EMAIL = "test@test2.org.uk";
    String PRIVATE_SOLICITOR_USER_EMAIL = "test@private.solicitors.uk";
    String PRIVATE_ORG_ID = "ORG-EXT";
    String DEFAULT_LA_COURT = "Family Court";
    String DEFAULT_CAFCASS_COURT = "cafcass";
    String DEFAULT_CAFCASS_EMAIL = "FamilyPublicLaw+cafcass@gmail.com";
    String DEFAULT_CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";
    String DEFAULT_ADMIN_EMAIL = "admin@family-court.com";
    String USER_AUTH_TOKEN = "token";
}
