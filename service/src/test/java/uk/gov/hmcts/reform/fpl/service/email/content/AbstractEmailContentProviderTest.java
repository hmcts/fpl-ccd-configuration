package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest.BASE_URL;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
@TestPropertySource(properties = {"ccd.ui.base.url=" + BASE_URL})
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
