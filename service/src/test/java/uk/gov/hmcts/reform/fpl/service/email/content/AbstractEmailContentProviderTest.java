package uk.gov.hmcts.reform.fpl.service.email.content;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

abstract class AbstractEmailContentProviderTest {
    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    static final String LOCAL_AUTHORITY_CODE = "example";
    static final String CAFCASS_NAME = "cafcass";
    static final String CASE_REFERENCE = "12345";
    static final String BASE_URL = "http://fake-url";
    static final String COURT_NAME = "Family Court";

    String buildCaseUrl(String caseId) {
        return formatCaseUrl(BASE_URL, Long.parseLong(caseId));
    }
}
