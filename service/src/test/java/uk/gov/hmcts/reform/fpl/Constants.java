package uk.gov.hmcts.reform.fpl;

import uk.gov.hmcts.reform.fpl.model.Court;

public interface Constants {

    String LOCAL_AUTHORITY_1_CODE = "test1";
    String LOCAL_AUTHORITY_2_CODE = "test2";
    String LOCAL_AUTHORITY_3_CODE = "test3";
    String LOCAL_AUTHORITY_1_NAME = "Test 1 Local Authority";
    String LOCAL_AUTHORITY_2_NAME = "Test 2 Local Authority";
    String LOCAL_AUTHORITY_3_NAME = "Test 3 Local Authority";
    String LOCAL_AUTHORITY_1_ID = "ORG-LA";
    String LOCAL_AUTHORITY_2_ID = "ORG-LA-2";
    String LOCAL_AUTHORITY_1_INBOX = "shared@test1.org.uk";
    String LOCAL_AUTHORITY_2_INBOX = "shared@test2.org.uk";
    String LOCAL_AUTHORITY_3_INBOX = "shared@test3.org.uk";
    String LOCAL_AUTHORITY_1_USER_EMAIL = "test@test1.org.uk";
    String LOCAL_AUTHORITY_2_USER_EMAIL = "test@test2.org.uk";
    String LOCAL_AUTHORITY_3_USER_EMAIL = "test@test3.org.uk";
    String PRIVATE_SOLICITOR_USER_EMAIL = "test@private.solicitors.uk";
    String PRIVATE_ORG_ID = "ORG-EXT";
    String DEFAULT_LA_COURT = "Family Court";
    String DEFAULT_CAFCASS_COURT = "cafcass";
    String DEFAULT_CAFCASS_EMAIL = "FamilyPublicLaw+cafcass@gmail.com";
    String DEFAULT_CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";
    String DEFAULT_ADMIN_EMAIL = "court1@family-court.com";
    String USER_AUTH_TOKEN = "token";
    String LOCAL_AUTHORITY_1_COURT_ID = "11";
    String LOCAL_AUTHORITY_3_COURT_A_ID = "31";
    String LOCAL_AUTHORITY_3_COURT_B_ID = "32";
    String LOCAL_AUTHORITY_1_COURT_NAME = "Family Court";
    String LOCAL_AUTHORITY_3_COURT_A_NAME = "Family Court 3 A";
    String LOCAL_AUTHORITY_3_COURT_B_NAME = "Family Court 3 B";
    String LOCAL_AUTHORITY_1_COURT_EMAIL = "court1@family-court.com";
    String LOCAL_AUTHORITY_3_COURT_A_EMAIL = "court3a@family-court.com";
    String LOCAL_AUTHORITY_3_COURT_B_EMAIL = "court3b@family-court.com";
    Court COURT_1 = Court.builder()
        .code("11")
        .name("Family Court")
        .email("court1@family-court.com")
        .build();
    Court COURT_2 = Court.builder()
        .code("22")
        .name("Family Court 2")
        .email("court2@family-court.com")
        .build();
    Court COURT_3A = Court.builder()
        .code("31")
        .name("Family Court 3 A")
        .email("court3a@family-court.com")
        .build();
    Court COURT_3B = Court.builder()
        .code("32")
        .name("Family Court 3 B")
        .email("court3b@family-court.com")
        .build();
    String USER_ID = "456";
    String TEST_CASE_ID = "1234123412341234";
    String TEST_FORMATTED_CASE_ID = "1234-1234-1234-1234";
    Long TEST_CASE_ID_AS_LONG = 1234123412341234L;

}
